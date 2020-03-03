package org.graalvm.wasm.nodes.control;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.Assert;
import org.graalvm.wasm.SymbolTable;
import org.graalvm.wasm.ValueTypes;
import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmFunction;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmTrap;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class WasmIndirectCallWrapperNode extends WasmNode {
	
	@CompilationFinal private final int expectedFunctionTypeIndex;
	@CompilationFinal private final WasmIndirectCallNode callNode;

	public WasmIndirectCallWrapperNode(WasmModule wasmModule, WasmCodeEntry codeEntry, int expectedFunctionTypeIndex, WasmIndirectCallNode callNode) {
		super(wasmModule, codeEntry);
		this.callNode = callNode;
		this.expectedFunctionTypeIndex = expectedFunctionTypeIndex;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		// Extract the function object.
        context.stackpointer--;
        final SymbolTable symtab = module().symbolTable();
        final Object[] elements = symtab.table().elements();
        final int elementIndex = popInt(frame, context.stackpointer);
        if (elementIndex < 0 || elementIndex >= elements.length) {
            throw new WasmTrap(this, "Element index '" + elementIndex + "' out of table bounds.");
        }
        // Currently, table elements may only be functions.
        // We can add a check here when this changes in the future.
        final WasmFunction function = (WasmFunction) elements[elementIndex];
        if (function == null) {
            throw new WasmTrap(this, "Table element at index " + elementIndex + " is uninitialized.");
        }

        // Extract the function type index.
        int expectedTypeEquivalenceClass = symtab.equivalenceClass(expectedFunctionTypeIndex);

        // Validate that the function type matches the expected type.
        if (expectedTypeEquivalenceClass != function.typeEquivalenceClass()) {
            // TODO: This check may be too rigorous, as the WebAssembly specification
            // seems to allow multiple definitions of the same type.
            // We should refine the check.
            throw new WasmTrap(this, Assert.format("Actual (type %d of function %s) and expected (type %d in module %s) types differ in the indirect call.",
                            function.typeIndex(), function.name(), expectedFunctionTypeIndex, module().name()));
        }

        int numArgs = module().symbolTable().functionTypeArgumentCount(expectedFunctionTypeIndex);
        Object[] args = createArgumentsForCall(frame, function, numArgs, context.stackpointer);
        context.stackpointer -= args.length;

        trace("indirect call to function %s (%d args)", function, args.length);
        Object result = callNode.execute(function, args);
        trace("return from indirect_call to function %s : %s", function, result);
        // At the moment, WebAssembly functions may return up to one value.
        // As per the WebAssembly specification, this restriction may be lifted in the
        // future.
        int returnType = module().symbolTable().functionTypeReturnType(expectedFunctionTypeIndex);
        switch (returnType) {
            case ValueTypes.I32_TYPE: {
                pushInt(frame, context.stackpointer, (int) result);
                context.stackpointer++;
                break;
            }
            case ValueTypes.I64_TYPE: {
                push(frame, context.stackpointer, (long) result);
                context.stackpointer++;
                break;
            }
            case ValueTypes.F32_TYPE: {
                pushFloat(frame, context.stackpointer, (float) result);
                context.stackpointer++;
                break;
            }
            case ValueTypes.F64_TYPE: {
                pushDouble(frame, context.stackpointer, (double) result);
                context.stackpointer++;
                break;
            }
            case ValueTypes.VOID_TYPE: {
                // Void return type - do nothing.
                break;
            }
            default: {
                throw new WasmTrap(this, "Unknown return type: " + returnType);
            }
        }
		return null;
	}
	
	@ExplodeLoop
    private Object[] createArgumentsForCall(VirtualFrame frame, WasmFunction function, int numArgs, int stackPointerOffset) {
        CompilerAsserts.partialEvaluationConstant(numArgs);
        Object[] args = new Object[numArgs];
        int stackPointer = stackPointerOffset;
        for (int i = numArgs - 1; i >= 0; --i) {
            stackPointer--;
            byte type = module().symbolTable().functionTypeArgumentTypeAt(function.typeIndex(), i);
            switch (type) {
                case ValueTypes.I32_TYPE:
                    args[i] = popInt(frame, stackPointer);
                    break;
                case ValueTypes.I64_TYPE:
                    args[i] = pop(frame, stackPointer);
                    break;
                case ValueTypes.F32_TYPE:
                    args[i] = popAsFloat(frame, stackPointer);
                    break;
                case ValueTypes.F64_TYPE:
                    args[i] = popAsDouble(frame, stackPointer);
                    break;
                default: {
                    throw new WasmTrap(this, "Unknown type: " + type);
                }
            }
        }
        return args;
    }

	@Override
	public byte returnTypeId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int byteConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int intConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int longConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int branchTableLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}

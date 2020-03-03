package org.graalvm.wasm.nodes.control;

import static org.graalvm.wasm.WasmTracing.trace;

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
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class WasmDirectCallNode extends WasmNode {
	
	@CompilationFinal private final int functionIndex;
	@CompilationFinal private final DirectCallNode callNode;

	public WasmDirectCallNode(WasmModule wasmModule, WasmCodeEntry codeEntry, int functionIndex, DirectCallNode callNode) {
		super(wasmModule, codeEntry);
		this.callNode = callNode;
		this.functionIndex = functionIndex;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		WasmFunction function = module().symbolTable().function(functionIndex);
        byte returnType = function.returnType();
        int numArgs = function.numArguments();

        Object[] args = createArgumentsForCall(frame, function, numArgs, context.stackpointer);
        context.stackpointer -= args.length;

        trace("direct call to function %s (%d args)", function, args.length);
        Object result = callNode.call(args);
        trace("return from direct call to function %s : %s", function, result);
        // At the moment, WebAssembly functions may return up to one value.
        // As per the WebAssembly specification,
        // this restriction may be lifted in the future.
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

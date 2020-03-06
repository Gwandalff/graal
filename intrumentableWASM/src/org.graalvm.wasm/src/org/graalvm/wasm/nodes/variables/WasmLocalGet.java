package org.graalvm.wasm.nodes.variables;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.ValueTypes;
import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmTrap;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmLocalGet extends WasmNode {
	
	@CompilationFinal private final int index;

	public WasmLocalGet(WasmModule wasmModule, WasmCodeEntry codeEntry, int index) {
		super(wasmModule, codeEntry);
		this.index = index;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		byte type = codeEntry().localType(index);
        switch (type) {
            case ValueTypes.I32_TYPE: {
                int value = getInt(frame, index);
                pushInt(frame, context.stackpointer, value);
                context.stackpointer++;
                trace("local.get %d, value = 0x%08X (%d) [i32]", index, value, value);
                break;
            }
            case ValueTypes.I64_TYPE: {
                long value = getLong(frame, index);
                push(frame, context.stackpointer, value);
                context.stackpointer++;
                trace("local.get %d, value = 0x%016X (%d) [i64]", index, value, value);
                break;
            }
            case ValueTypes.F32_TYPE: {
                float value = getFloat(frame, index);
                pushFloat(frame, context.stackpointer, value);
                context.stackpointer++;
                trace("local.get %d, value = %f [f32]", index, value);
                break;
            }
            case ValueTypes.F64_TYPE: {
                double value = getDouble(frame, index);
                pushDouble(frame, context.stackpointer, value);
                context.stackpointer++;
                trace("local.get %d, value = %f [f64]", index, value);
                break;
            }
            default: {
                throw new WasmTrap(this, "Local variable cannot have the void type.");
            }
        }
        return null;
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

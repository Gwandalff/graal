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

public class WasmLocalSet extends WasmNode {

	@CompilationFinal private final byte type;
	@CompilationFinal private final int index;

	public WasmLocalSet(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type, int index) {
		super(wasmModule, codeEntry);
		this.type = type;
		this.index = index;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
        case ValueTypes.I32_TYPE: {
            context.stackpointer--;
            int value = popInt(frame, context.stackpointer);
            setInt(frame, index, value);
            trace("local.set %d, value = 0x%08X (%d) [i32]", index, value, value);
            break;
        }
        case ValueTypes.I64_TYPE: {
            context.stackpointer--;
            long value = pop(frame, context.stackpointer);
            setLong(frame, index, value);
            trace("local.set %d, value = 0x%016X (%d) [i64]", index, value, value);
            break;
        }
        case ValueTypes.F32_TYPE: {
            context.stackpointer--;
            float value = popAsFloat(frame, context.stackpointer);
            setFloat(frame, index, value);
            trace("local.set %d, value = %f [f32]", index, value);
            break;
        }
        case ValueTypes.F64_TYPE: {
            context.stackpointer--;
            double value = popAsDouble(frame, context.stackpointer);
            setDouble(frame, index, value);
            trace("local.set %d, value = %f [f64]", index, value);
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

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

public class WasmGlobalSet extends WasmNode {

	@CompilationFinal private final int index;

	public WasmGlobalSet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, int index) {
		super(wasmModule, codeEntry, byteLength);
		this.index = index;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		byte type = module().symbolTable().globalValueType(index);
		switch (type) {
        case ValueTypes.I32_TYPE: {
            context.stackpointer--;
            int value = popInt(frame, context.stackpointer);
            int address = module().symbolTable().globalAddress(index);
            context.globals().storeInt(address, value);
            trace("global.set %d, value = 0x%08X (%d) [i32]", index, value, value);
            break;
        }
        case ValueTypes.I64_TYPE: {
            context.stackpointer--;
            long value = pop(frame, context.stackpointer);
            int address = module().symbolTable().globalAddress(index);
            context.globals().storeLong(address, value);
            trace("global.set %d, value = 0x%016X (%d) [i64]", index, value, value);
            break;
        }
        case ValueTypes.F32_TYPE: {
            context.stackpointer--;
            int value = popInt(frame, context.stackpointer);
            int address = module().symbolTable().globalAddress(index);
            context.globals().storeFloatWithInt(address, value);
            trace("global.set %d, value = %f [f32]", index, Float.intBitsToFloat(value));
            break;
        }
        case ValueTypes.F64_TYPE: {
            context.stackpointer--;
            long value = pop(frame, context.stackpointer);
            int address = module().symbolTable().globalAddress(index);
            context.globals().storeDoubleWithLong(address, value);
            trace("global.set %d, value = %f [f64]", index, Double.longBitsToDouble(value));
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

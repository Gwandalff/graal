package org.graalvm.wasm.nodes.comparison;

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

public class WasmEQ extends WasmNode {

	@CompilationFinal
	private final byte type;
	
	public WasmEQ(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, byte type) {
		super(wasmModule, codeEntry, byteLength);
		this.type = type;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.I32_TYPE: {
			context.stackpointer--;
            int x = popInt(frame, context.stackpointer);
            context.stackpointer--;
            int y = popInt(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, y == x ? 1 : 0);
            context.stackpointer++;
            trace("0x%016X == 0x%016X ? [i32]", y, x);
			break;
		}
		case ValueTypes.I64_TYPE: {
			context.stackpointer--;
            long x = pop(frame, context.stackpointer);
            context.stackpointer--;
            long y = pop(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, y == x ? 1 : 0);
            context.stackpointer++;
            trace("0x%016X == 0x%016X ? [i64]", y, x);
			break;
		}
		case ValueTypes.F32_TYPE: {
			context.stackpointer--;
            float x = popAsFloat(frame, context.stackpointer);
            context.stackpointer--;
            float y = popAsFloat(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, y == x ? 1 : 0);
            context.stackpointer++;
            trace("%f == %f ? [f32]", y, x);
			break;
		}
		case ValueTypes.F64_TYPE: {
			context.stackpointer--;
            double x = popAsDouble(frame, context.stackpointer);
            context.stackpointer--;
            double y = popAsDouble(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, y == x ? 1 : 0);
            context.stackpointer++;
            trace("%f == %f ? [f64]", y, x);
			break;
		}
		default:
			throw new WasmTrap(this, "EQ cannot have the void type.");
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

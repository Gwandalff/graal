package org.graalvm.wasm.nodes.numeric;

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

public class WasmNeg extends WasmNode {

	@CompilationFinal
	private final byte type;

	public WasmNeg(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, byte type) {
		super(wasmModule, codeEntry, byteLength);
		this.type = type;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.F32_TYPE: {
			context.stackpointer--;
			float x = popAsFloat(frame, context.stackpointer);
			float result = -x;
			pushFloat(frame, context.stackpointer, result);
			context.stackpointer++;
			trace("f32.neg(%f) = %f", x, result);
			break;
		}
		case ValueTypes.F64_TYPE: {
			context.stackpointer--;
			double x = popAsDouble(frame, context.stackpointer);
			double result = -x;
			pushDouble(frame, context.stackpointer, result);
			context.stackpointer++;
			trace("f64.neg(%f) = %f", x, result);
			break;
		}

		default:
			throw new WasmTrap(this, "Neg need the F** type.");
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

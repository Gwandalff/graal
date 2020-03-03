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

public class WasmMin extends WasmNode {

	@CompilationFinal
	private final byte type;

	public WasmMin(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type) {
		super(wasmModule, codeEntry);
		this.type = type;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.F32_TYPE: {
			context.stackpointer--;
            float x = popAsFloat(frame, context.stackpointer);
            context.stackpointer--;
            float y = popAsFloat(frame, context.stackpointer);
            float result = Math.min(y, x);
            pushFloat(frame, context.stackpointer, result);
            context.stackpointer++;
            trace("push min(%f, %f) = %f [f32]", y, x, result);
            break;
		}
		case ValueTypes.F64_TYPE: {
			context.stackpointer--;
            double x = popAsDouble(frame, context.stackpointer);
            context.stackpointer--;
            double y = popAsDouble(frame, context.stackpointer);
            double result = Math.min(y, x);
            pushDouble(frame, context.stackpointer, result);
            context.stackpointer++;
            trace("push min(%f, %f) = %f [f64]", y, x, result);
            break;
		}

		default:
			throw new WasmTrap(this, "Min need the F** type.");
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

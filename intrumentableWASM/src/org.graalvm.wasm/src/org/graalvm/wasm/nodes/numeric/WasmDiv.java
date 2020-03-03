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

public class WasmDiv extends WasmNode {

	@CompilationFinal private final byte type;
	@CompilationFinal private final boolean signed;

	public WasmDiv(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type) {
		super(wasmModule, codeEntry);
		this.type = type;
		this.signed = true;
	}
	
	public WasmDiv(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type, boolean signed) {
		super(wasmModule, codeEntry);
		this.type = type;
		this.signed = signed;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.I32_TYPE: {
			context.stackpointer--;
			int x = popInt(frame, context.stackpointer);
			context.stackpointer--;
			int y = popInt(frame, context.stackpointer);
			int result = (signed ? y / x : Integer.divideUnsigned(y, x));
			pushInt(frame, context.stackpointer, result);
			context.stackpointer++;
			trace("push 0x%08X / 0x%08X = 0x%08X (%d) [i32]", y, x, result, result);
			break;
		}
		case ValueTypes.I64_TYPE: {
			context.stackpointer--;
			long x = pop(frame, context.stackpointer);
			context.stackpointer--;
			long y = pop(frame, context.stackpointer);
			long result = (signed ? y / x : Long.divideUnsigned(y, x));
			push(frame, context.stackpointer, result);
			context.stackpointer++;
			trace("push 0x%016X / 0x%016X = 0x%016X (%d) [i64]", y, x, result, result);
			break;
		}
		case ValueTypes.F32_TYPE: {
			context.stackpointer--;
			float x = popAsFloat(frame, context.stackpointer);
			context.stackpointer--;
			float y = popAsFloat(frame, context.stackpointer);
			float result = y / x;
			pushFloat(frame, context.stackpointer, result);
			trace("push %f / %f = %f [f32]", y, x, result);
			context.stackpointer++;
			break;
		}
		case ValueTypes.F64_TYPE: {
			context.stackpointer--;
			double x = popAsDouble(frame, context.stackpointer);
			context.stackpointer--;
			double y = popAsDouble(frame, context.stackpointer);
			double result = y / x;
			pushDouble(frame, context.stackpointer, result);
			context.stackpointer++;
			trace("push %f / %f = %f [f64]", y, x, result);
			break;
		}
		default:
			throw new WasmTrap(this, "Div cannot have the void type.");
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

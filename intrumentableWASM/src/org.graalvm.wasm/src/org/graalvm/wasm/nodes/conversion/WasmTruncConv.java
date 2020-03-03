package org.graalvm.wasm.nodes.conversion;

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

public class WasmTruncConv extends WasmNode {

	@CompilationFinal private final byte fromT;
	@CompilationFinal private final byte toT;
	
	public WasmTruncConv(WasmModule wasmModule, WasmCodeEntry codeEntry, byte from, byte to) {
		super(wasmModule, codeEntry);
		this.fromT = from;
		this.toT = to;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
		switch (fromT) {
		case ValueTypes.F32_TYPE:
			float xFloat = popAsFloat(frame, context.stackpointer);
			switch (toT) {
			case ValueTypes.I32_TYPE:
				int resultInt = (int) xFloat;
                pushInt(frame, context.stackpointer, resultInt);
                context.stackpointer++;
                trace("push trunc_f32(%f) = 0x%08X (%d) [i32]", xFloat, resultInt, resultInt);
				break;
			case ValueTypes.I64_TYPE:
				long resultLong = (long) xFloat;
                push(frame, context.stackpointer, resultLong);
                context.stackpointer++;
                trace("push trunc_f32(%f) = 0x%08X (%d) [i64]", xFloat, resultLong, resultLong);
				break;

			default:
				throw new WasmTrap(this, "TruncConv need a I** type as output.");
			}
			
			break;
		case ValueTypes.F64_TYPE:
			double xDouble = popAsDouble(frame, context.stackpointer);
			switch (toT) {
			case ValueTypes.I32_TYPE:
				int resultInt = (int) xDouble;
                pushInt(frame, context.stackpointer, resultInt);
                context.stackpointer++;
                trace("push trunc_f64(%f) = 0x%08X (%d) [i32]", xDouble, resultInt, resultInt);
				break;
			case ValueTypes.I64_TYPE:
				long resultLong = (long) xDouble;
                push(frame, context.stackpointer, resultLong);
                context.stackpointer++;
                trace("push trunc_f64(%f) = 0x%08X (%d) [i64]", xDouble, resultLong, resultLong);
				break;

			default:
				throw new WasmTrap(this, "TruncConv need a I** type as output.");
			}
			
			break;

		default:
			throw new WasmTrap(this, "TruncConv need a F** type as input.");
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

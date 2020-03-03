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

public class WasmConvert extends WasmNode {

	@CompilationFinal private final byte fromT;
	@CompilationFinal private final byte toT;
	
	public WasmConvert(WasmModule wasmModule, WasmCodeEntry codeEntry, byte from, byte to) {
		super(wasmModule, codeEntry);
		this.fromT = from;
		this.toT = to;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
		switch (fromT) {
		case ValueTypes.I32_TYPE:
			int xInt = popInt(frame, context.stackpointer);
			switch (toT) {
			case ValueTypes.F32_TYPE:
				float resultFloat =  xInt;
                pushFloat(frame, context.stackpointer, resultFloat);
                context.stackpointer++;
                trace("push conv_i32(%f) = 0x%08X (%d) [f32]", xInt, resultFloat, resultFloat);
				break;
			case ValueTypes.F64_TYPE:
				double resultDouble =  xInt;
                pushDouble(frame, context.stackpointer, resultDouble);
                context.stackpointer++;
                trace("push conv_i32(%f) = 0x%08X (%d) [f64]", xInt, resultDouble, resultDouble);
				break;

			default:
				throw new WasmTrap(this, "Convert need a F** type as output.");
			}
			
			break;
		case ValueTypes.I64_TYPE:
			long xLong = pop(frame, context.stackpointer);
			switch (toT) {
			case ValueTypes.F32_TYPE:
				float resultFloat =  xLong;
                pushFloat(frame, context.stackpointer, resultFloat);
                context.stackpointer++;
                trace("push conv_i64(%f) = 0x%08X (%d) [f32]", xLong, resultFloat, resultFloat);
				break;
			case ValueTypes.F64_TYPE:
				double resultDouble =  xLong;
                pushDouble(frame, context.stackpointer, resultDouble);
                context.stackpointer++;
                trace("push conv_i64(%f) = 0x%08X (%d) [f64]", xLong, resultDouble, resultDouble);
				break;

			default:
				throw new WasmTrap(this, "Convert need a F** type as output.");
			}
			
			break;

		default:
			throw new WasmTrap(this, "Convert need a I** type as input.");
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

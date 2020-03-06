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

public class WasmConst extends WasmNode {
	
	@CompilationFinal private final byte type;
	@CompilationFinal private final int intvalue;
	@CompilationFinal private final long longvalue;

	public WasmConst(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type, int value) {
		super(wasmModule, codeEntry);
		this.type = type;
		this.intvalue = value;
		this.longvalue = 0;
	}
	public WasmConst(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type, long value) {
		super(wasmModule, codeEntry);
		this.type = type;
		this.intvalue = 0;
		this.longvalue = value;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (this.type) {
		case ValueTypes.F32_TYPE:
			pushInt(frame, context.stackpointer, this.intvalue);
			context.stackpointer++;
            trace("f32.const %f", Float.intBitsToFloat(this.intvalue));
            //System.out.println("F32 const : "+Float.intBitsToFloat(this.intvalue)+" -> SP :" + context.stackpointer);
			break;
		case ValueTypes.F64_TYPE:
			push(frame, context.stackpointer, this.longvalue);
			context.stackpointer++;
            trace("f64.const %f", Double.longBitsToDouble(this.longvalue));
            //System.out.println("F64 const : "+Double.longBitsToDouble(this.longvalue)+" -> SP :" + context.stackpointer);
			break;
		case ValueTypes.I32_TYPE:
			pushInt(frame, context.stackpointer, this.intvalue);
			context.stackpointer++;
            trace("i32.const 0x%08X (%d)", this.intvalue, this.intvalue);
            //System.out.println("I32 const : "+intvalue+" -> SP :" + context.stackpointer);
			break;
		case ValueTypes.I64_TYPE:
			push(frame, context.stackpointer, this.longvalue);
			context.stackpointer++;
            trace("i64.const 0x%08X (%d)", this.longvalue, this.longvalue);
            //System.out.println("I64 const : "+longvalue+" -> SP :" + context.stackpointer);
			break;

		default:
			throw new WasmTrap(this, "Constant cannot have the void type.");
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

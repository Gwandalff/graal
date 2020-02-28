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

public class WasmEQZ extends WasmNode {

	@CompilationFinal
	private final byte type;
	
	public WasmEQZ(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, byte type) {
		super(wasmModule, codeEntry, byteLength);
		this.type = type;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.I32_TYPE: {
			context.stackpointer--;
            int x = popInt(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, x == 0 ? 1 : 0);
            context.stackpointer++;
            trace("0x%08X == 0x%08X ? [i32]", x, 0);
			break;
		}
		case ValueTypes.I64_TYPE: {
			context.stackpointer--;
            long x = pop(frame, context.stackpointer);
            pushInt(frame, context.stackpointer, x == 0 ? 1 : 0);
            context.stackpointer++;
            trace("0x%016X == 0x%016X ? [i64]", x, 0);
			break;
		}
		default:
			throw new WasmTrap(this, "EQZ need the I** type.");
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

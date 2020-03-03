package org.graalvm.wasm.nodes.bitsOp;

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

public class WasmCLZ extends WasmNode {

	@CompilationFinal private final byte type;

	public WasmCLZ(WasmModule wasmModule, WasmCodeEntry codeEntry, byte type) {
		super(wasmModule, codeEntry);
		this.type = type;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		switch (type) {
		case ValueTypes.I32_TYPE: {
			context.stackpointer--;
            int x = popInt(frame, context.stackpointer);
            int result = Integer.numberOfLeadingZeros(x);
            pushInt(frame, context.stackpointer, result);
            context.stackpointer++;
            trace("clz(0x%08X) = %d [i32]", x, result);
			break;
		}
		case ValueTypes.I64_TYPE: {
			context.stackpointer--;
            long x = pop(frame, context.stackpointer);
            long result = Long.numberOfLeadingZeros(x);
            push(frame, context.stackpointer, result);
            context.stackpointer++;
            trace("clz(0x%016X) = 0x%08X (%d) [i64]", x, result, result);
			break;
		}
		default:
			throw new WasmTrap(this, "CLZ need the I** type.");
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

package org.graalvm.wasm.nodes.conversion;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmWrap extends WasmNode {

	public WasmWrap(WasmModule wasmModule, WasmCodeEntry codeEntry) {
		super(wasmModule, codeEntry);
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
        long x = pop(frame, context.stackpointer);
        int result = (int) (x & 0xFFFF_FFFFL);
        pushInt(frame, context.stackpointer, result);
        context.stackpointer++;
        trace("push wrap_i64(0x%016X) = 0x%08X (%d) [i32]", x, result, result);
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

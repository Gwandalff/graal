package org.graalvm.wasm.nodes.memory;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmMemorySize extends WasmNode {

	public WasmMemorySize(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength) {
		super(wasmModule, codeEntry, byteLength);
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		trace("memory_size");
        int pageSize = (int) (module().symbolTable().memory().pageSize());
        pushInt(frame, context.stackpointer, pageSize);
        context.stackpointer++;
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

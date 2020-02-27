package org.graalvm.wasm.nodes.variables;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmGlobalSet extends WasmNode {

	public WasmGlobalSet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength) {
		super(wasmModule, codeEntry, byteLength);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		// TODO Auto-generated method stub
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

package org.graalvm.wasm.nodes.conversion;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WasmDemote extends WasmNode {

	public WasmDemote(WasmModule wasmModule, WasmCodeEntry codeEntry) {
		super(wasmModule, codeEntry);
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		throw new NotImplementedException();
		//return null;
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

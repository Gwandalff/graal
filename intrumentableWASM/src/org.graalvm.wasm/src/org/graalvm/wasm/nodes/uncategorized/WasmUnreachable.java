package org.graalvm.wasm.nodes.uncategorized;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmTrap;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmUnreachable extends WasmNode {

	public WasmUnreachable(WasmModule wasmModule, WasmCodeEntry codeEntry) {
		super(wasmModule, codeEntry);
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		trace("unreachable");
        throw new WasmTrap(this, "unreachable");
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

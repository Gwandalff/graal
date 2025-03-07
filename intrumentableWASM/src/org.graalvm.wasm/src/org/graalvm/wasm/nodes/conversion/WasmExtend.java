package org.graalvm.wasm.nodes.conversion;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmExtend extends WasmNode {
	
	@CompilationFinal private final boolean signed;

	public WasmExtend(WasmModule wasmModule, WasmCodeEntry codeEntry, boolean signed) {
		super(wasmModule, codeEntry);
		this.signed = signed;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
        int x = popInt(frame, context.stackpointer);
        long result = this.signed ? x : x & 0xFFFF_FFFFL;
        push(frame, context.stackpointer, result);
        context.stackpointer++;
        trace("push extend_i32_" + (signed?"s":"u") + "(0x%08X) = 0x%016X (%d) [i64]", x, result, result);
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

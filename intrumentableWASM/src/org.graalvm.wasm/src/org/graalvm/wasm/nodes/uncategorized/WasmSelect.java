package org.graalvm.wasm.nodes.uncategorized;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmSelect extends WasmNode {

	public WasmSelect(WasmModule wasmModule, WasmCodeEntry codeEntry) {
		super(wasmModule, codeEntry);
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
        int cond = popInt(frame, context.stackpointer);
        context.stackpointer--;
        long val2 = pop(frame, context.stackpointer);
        context.stackpointer--;
        long val1 = pop(frame, context.stackpointer);
        push(frame, context.stackpointer, cond != 0 ? val1 : val2);
        context.stackpointer++;
        trace("select 0x%08X ? 0x%08X : 0x%08X = 0x%08X", cond, val1, val2, cond != 0 ? val1 : val2);
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

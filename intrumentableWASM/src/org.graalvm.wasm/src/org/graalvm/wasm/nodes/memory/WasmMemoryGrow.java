package org.graalvm.wasm.nodes.memory;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.memory.WasmMemory;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmMemoryGrow extends WasmNode {

	public WasmMemoryGrow(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength) {
		super(wasmModule, codeEntry, byteLength);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		trace("memory_grow");
        context.stackpointer--;
        int extraSize = popInt(frame, context.stackpointer);
        final WasmMemory memory = module().symbolTable().memory();
        int pageSize = (int) memory.pageSize();
        if (memory.grow(extraSize)) {
            pushInt(frame, context.stackpointer, pageSize);
            context.stackpointer++;
        } else {
            pushInt(frame, context.stackpointer, -1);
            context.stackpointer++;
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

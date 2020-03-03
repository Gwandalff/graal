package org.graalvm.wasm.nodes.control;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmExecutionException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RepeatingNode;

public class WasmLoopNode extends WasmBlockNode implements RepeatingNode {

	public WasmLoopNode(WasmModule wasmModule, WasmCodeEntry codeEntry, int startOffset, byte returnTypeId,
			byte continuationTypeId, int initialStackPointer, int initialByteConstantOffset,
			int initialIntConstantOffset, int initialLongConstantOffset, int initialBranchTableOffset) {
		super(wasmModule, codeEntry, startOffset, returnTypeId, continuationTypeId, initialStackPointer,
				initialByteConstantOffset, initialIntConstantOffset, initialLongConstantOffset, initialBranchTableOffset, false);
	}
	
	
	
	@Override
    public boolean executeRepeating(VirtualFrame frame) {
        throw new WasmExecutionException(this, "This method should never have been called.");
    }

    @Override
    public Object executeRepeatingWithValue(VirtualFrame frame) {
        final TargetOffset offset = execute(contextReference().get(), frame);
        if (offset.isZero()) {
            return CONTINUE_LOOP_STATUS;
        }
        return offset.isMinusOne() ? offset : offset.decrement();
    }

}

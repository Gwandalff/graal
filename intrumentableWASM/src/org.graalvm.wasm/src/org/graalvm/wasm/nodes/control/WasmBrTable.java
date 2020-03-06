package org.graalvm.wasm.nodes.control;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class WasmBrTable extends WasmNode {

	@CompilationFinal private final int branchTableOffset;

	public WasmBrTable(WasmModule wasmModule, WasmCodeEntry codeEntry, int branchTableOffset) {
		super(wasmModule, codeEntry);
		this.branchTableOffset = branchTableOffset;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
        int index = popInt(frame, context.stackpointer);
        int[] table = codeEntry().branchTable(branchTableOffset);
        index = index < 0 || index >= (table.length - 1) / 2 ? (table.length - 1) / 2 - 1 : index;
        // Technically, we should increment the branchTableOffset at this point,
        // but since we are returning, it does not really matter.

        int returnTypeLength = table[0];
        int unwindCounterValue = table[1 + 2 * index]+1;
        int continuationStackPointer = table[1 + 2 * index + 1];
        trace("br_table, target = %d", unwindCounterValue);

        // Populate the stack with the return values of the current block (the one we
        // are escaping from).
        unwindStack(frame, context, context.stackpointer, continuationStackPointer, returnTypeLength);

        return TargetOffset.createOrCached(unwindCounterValue);
	}
	
	@ExplodeLoop
    private void unwindStack(VirtualFrame frame, WasmContext context, int initStackPointer, int initialContinuationStackPointer, int targetBlockReturnLength) {
        // TODO: If the targetBlockReturnLength could ever be > 1, this would invert the stack
        // values.
        // The spec seems to imply that the operand stack should not be inverted.
        CompilerAsserts.partialEvaluationConstant(targetBlockReturnLength);
        int stackPointer = initStackPointer;
        int continuationStackPointer = initialContinuationStackPointer;
        for (int i = 0; i != targetBlockReturnLength; ++i) {
            stackPointer--;
            long value = pop(frame, stackPointer);
            push(frame, continuationStackPointer, value);
            continuationStackPointer++;
        }
        context.stackpointer = continuationStackPointer;
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

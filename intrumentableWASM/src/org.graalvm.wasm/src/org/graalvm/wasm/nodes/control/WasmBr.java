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

public class WasmBr extends WasmNode {
	
	@CompilationFinal private final int unwindLevel;
	@CompilationFinal private final int continuationStackPointer;
	@CompilationFinal private final int targetBlockReturnLength;

	public WasmBr(WasmModule wasmModule, WasmCodeEntry codeEntry, int unwindLevel, int continuationStackPointer, int targetBlockReturnLength) {
		super(wasmModule, codeEntry);
		this.unwindLevel = unwindLevel;
		this.continuationStackPointer = continuationStackPointer;
		this.targetBlockReturnLength = targetBlockReturnLength;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		trace("br, target = %d", unwindLevel);
		unwindStack(frame, context.stackpointer, continuationStackPointer, targetBlockReturnLength);
		return TargetOffset.createOrCached(unwindLevel);
	}
	
	@ExplodeLoop
    private void unwindStack(VirtualFrame frame, int initStackPointer, int initialContinuationStackPointer, int targetBlockReturnLength) {
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

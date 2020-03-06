package org.graalvm.wasm.nodes.control;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmExecutionException;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RepeatingNode;

public class WasmLoopNode extends WasmNode implements RepeatingNode {
	
	@CompilationFinal private final WasmBlockNode body;

	public WasmLoopNode(WasmModule wasmModule, WasmCodeEntry codeEntry, WasmBlockNode body) {
		super(wasmModule, codeEntry);
		this.body = body;
	}
	
	
	
	@Override
    public boolean executeRepeating(VirtualFrame frame) {
        throw new WasmExecutionException(this, "This method should never have been called.");
    }

    @Override
    public Object executeRepeatingWithValue(VirtualFrame frame) {
        final TargetOffset offset = execute(body.contextReference().get(), frame);
        if (offset.isZero()) {
            return CONTINUE_LOOP_STATUS;
        }
        return offset.isMinusOne() ? offset : offset.decrement();
    }



	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		TargetOffset offset = body.executeLoop(context, frame);
        while (offset.isOne()) {
        	offset = body.executeLoop(context, frame);
        }
		return offset.isMinusOne() || offset.isZero() ? offset : offset.decrement();
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

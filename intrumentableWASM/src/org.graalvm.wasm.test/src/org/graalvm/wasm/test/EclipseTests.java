package org.graalvm.wasm.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.wasm.launcher.WasmLauncher;
import org.junit.Test;

public class EclipseTests {
	
	public List<String> getFiles(String folder) {

		List<String> list = new ArrayList<String>();
        File dir = new File(folder);
        if(dir.isDirectory()) {
            FileFilter wasm = new FileFilter() {

                public boolean accept(File file) {
                    if(file.isFile() && !file.isDirectory()) {
                        String filename = file.getName();
                        return filename.endsWith(".wasm");
                    }
					return false;
                }

            };
            FileFilter folderFilter = new FileFilter() {
                public boolean accept(File file) {
					return file.isFile() && file.isDirectory();
                }
            };
            File[] dirs = dir.listFiles(folderFilter);
            File[] fileNames = dir.listFiles(wasm);
            for (File file : fileNames) {
                list.add(file.getAbsolutePath());
            }
            for (File d : dirs) {
				list.addAll(getFiles(d.getAbsolutePath()));
			}
        }
        return list;

	}

	@Test
	public void test() {
		List<String> wasm = getFiles("/home/ALE/graal/instrumentableWASM/test/");
		for (String path : wasm) {
			String[] args = {path};
			WasmLauncher.main(args);
			String expected = "ERROR";
			try {
				expected = new String ( Files.readAllBytes( Paths.get(path.replace(".wasm", ".result")) ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("expect : " + expected);
		}
	}

}

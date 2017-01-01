/*
 * Copyright (c) 2017 Richard Jones <richard@mechanicalcat.net>
 * All Rights Reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.mechanicalcat.pycode.obj;

import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Material {
    public String name = "";
    public Vector3f ambient = new Vector3f();
    public Vector3f diffuse = new Vector3f();
    public Vector3f specular = new Vector3f();
    public double specularNs = 0;
    public double opacity = 0;
    public int illuminationModel = 0;

    Material (String name) {
        this.name = name;
    }

    @Nullable
    public static Material loadMaterial(String filename) {
        File f = new File(filename, "r");
        try {
            return loadMaterial(f);
        } catch (Exception e) {
            System.out.println("Error loading material " + filename + ": " + e);
            return null;
        }
    }

    private static Material loadMaterial(File f) throws IOException, NullPointerException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Material m = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.contains(" ")) continue;
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "newmtl":
                    m = new Material(parts[1]);
                    break;
                case "Ka":
                    m.ambient = s2v(parts[1], parts[2], parts[3]);
                    break;
                case "Kd":
                    m.diffuse = s2v(parts[1], parts[2], parts[3]);
                    break;
                case "Ks":
                    m.specular = s2v(parts[1], parts[2], parts[3]);
                    break;
                case "Ns":
                    m.specularNs = Float.valueOf(parts[1]);
                    break;
                case "d":
                    m.opacity = Float.valueOf(parts[1]);
                    break;
                case "Tr":
                    m.opacity = 1 - Float.valueOf(parts[1]);
                    break;
                case "illum":
                    m.illuminationModel = Integer.valueOf(parts[1]);
            }
        }
        return m;
    }

    private static Vector3f s2v(String x, String y, String z) {
        return new Vector3f(Float.valueOf(x), Float.valueOf(y), Float.valueOf(z));
    }
}

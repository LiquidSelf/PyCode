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

package net.mechanicalcat.pycode;

import com.google.common.base.CharMatcher;
import net.minecraftforge.fml.common.FMLLog;
import org.python.jsr223.PyScriptEngine;

import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class PythonEngine
{
    private static PythonEngine instance = null;
    private PyScriptEngine engine;

    private PythonEngine()
    {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = (PyScriptEngine) manager.getEngineByName("python");

        try
        {
            engine.eval("print 'Python Ready'");
        }
        catch (ScriptException e)
        {
            e.printStackTrace();
            FMLLog.log.fatal("Python failed: %s", e);
        }
    }

    public static PythonEngine getEngine()
    {
        if (instance == null)
        {
            instance = new PythonEngine();
        }
        return instance;
    }

    public static void compile(String code) throws ScriptException
    {
        if (CharMatcher.ascii().matchesAllOf(code))
        {
            getEngine().engine.compile(code);
        }
        else
        {
            FMLLog.log.error("Кодировка не ASCII!");
        }
    }

    public static void eval(String code, ScriptContext context) throws ScriptException
    {
        getEngine().engine.eval(code, context);
    }
}
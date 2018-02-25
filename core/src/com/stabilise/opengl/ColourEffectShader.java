package com.stabilise.opengl;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class ColourEffectShader extends ShaderProgram {
    
    private static final String UNIFORM_TRANSMAT = "u_projTrans";
    private static final String UNIFORM_TEXTURE = "u_texture";
    
    public ColourEffectShader() {
        super(
                  "attribute vec4 " + POSITION_ATTRIBUTE + ";\n"
                + "attribute vec4 " + COLOR_ATTRIBUTE + ";\n"
                + "attribute vec2 " + TEXCOORD_ATTRIBUTE + ";\n"
                + "uniform mat4 " + UNIFORM_TRANSMAT + ";\n"
                + "varying vec4 vColour;\n"
                + "varying vec2 vTexCoords;\n"
                + "void main() {\n"
                + "    vColour = " + COLOR_ATTRIBUTE + ";\n"
                + "    vTexCoords = " + TEXCOORD_ATTRIBUTE + ";\n"
                + "    gl_Position =  " + UNIFORM_TRANSMAT + " * " + POSITION_ATTRIBUTE + ";\n"
                + "}",
                
                  "#ifdef GL_ES\n" // Taken from the libgdx default shader
                + "    #define LOWP lowp\n"
                + "    precision mediump float;\n"
                + "#else\n"
                + "    #define LOWP \n"
                + "#endif\n"
                + "varying LOWP vec4 vColour;\n"
                + "varying vec2 vTexCoords;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE + ";\n"
            //    + "uniform float opacity;\n"
            //    + "uniform float alpha;\n"
                + "void main() {\n"
            //    + "    vec4 texColor = texture2D(" + UNIFORM_TEXTURE + ", vTexCoords);\n"
            //    + "    gl_FragColor = vec4(texColor.rgb*tint*opacity, texColor.a*alpha);\n" +    // Multiplicative shader
            //    + "    gl_FragColor = vec4(texColor.rgb*(1.0-opacity) + tint*opacity, texColor.a*alpha);\n"        // Additive shader
            //    + "    gl_FragColor = vec4(texColor.rgb*(1.0-vColour.a) + vColour.rgb*vColour.a, texColor.a);\n"
                + "    gl_FragColor = vColour * texture2D(" + UNIFORM_TEXTURE + ", vTexCoords);\n"
                + "}"
        );
        
        // Default shader, for comparison
        /*
        String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
        String fragmentShader = "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "varying LOWP vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "void main()\n"//
                + "{\n" //
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
                + "}";
        */
        
        /*What the colour effect shader is supposed to be
            "attribute vec2 " + ATTRIBUTE_POSITION_NAME + ";\n" +
            "attribute vec4 " + ATTRIBUTE_COLOUR_NAME + ";\n" +
            "attribute vec2 " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            " vTexCoord = " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
            " gl_Position = ftransform();\n" +
            "}",
            
            "uniform sampler2D " + UNIFORM_TEXTURE_NAME + ";\n" +
            "uniform vec3 tint;\n" +
            "uniform float opacity;\n" +
            "uniform float alpha;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            " vec4 texColor = texture2D(" + UNIFORM_TEXTURE_NAME + ", vTexCoord);\n" +
            //" gl_FragColor = vec4(texColor.rgb*tint*opacity, texColor.a*alpha);\n" +    // Multiplicative shader
            " gl_FragColor = vec4(texColor.rgb*(1.0-opacity) + tint*opacity, texColor.a*alpha);\n" +        // Additive shader
            "}";
         */
    }
    
}

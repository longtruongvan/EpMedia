#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES tex_sampler;
varying vec2 vTexCoord;

// Width and height of a single pixel (1.0 / resolution)
uniform float texelWidthOffset;
uniform float texelHeightOffset;

void main() {
    // Unsharp mask or simple Laplacian edge enhancement kernel:
    // [  0, -1,  0 ]
    // [ -1,  5, -1 ]
    // [  0, -1,  0 ]
    
    vec2 offsetL = vec2(-texelWidthOffset, 0.0);
    vec2 offsetR = vec2(texelWidthOffset, 0.0);
    vec2 offsetT = vec2(0.0, -texelHeightOffset);
    vec2 offsetB = vec2(0.0, texelHeightOffset);

    vec4 center = texture2D(tex_sampler, vTexCoord);
    vec4 left   = texture2D(tex_sampler, vTexCoord + offsetL);
    vec4 right  = texture2D(tex_sampler, vTexCoord + offsetR);
    vec4 top    = texture2D(tex_sampler, vTexCoord + offsetT);
    vec4 bottom = texture2D(tex_sampler, vTexCoord + offsetB);

    vec4 color = center * 5.0 - (left + right + top + bottom);
    
    // Clamp values to prevent strange artifacts
    color = clamp(color, 0.0, 1.0);
    
    // Maintain original alpha
    color.a = center.a;
    
    gl_FragColor = color;
}

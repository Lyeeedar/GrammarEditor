#ifdef GL_ES
	precision mediump float;
#endif
			
uniform vec3 sky_col;
uniform vec3 col_shift;

uniform vec3 sun_col;
uniform vec3 sun_dir;
uniform float sun_size;

varying vec3 v_pos;

void main()
{
    vec3 V = normalize(v_pos);
    vec3 Vh = normalize(vec3(v_pos.x, 0, v_pos.z));
    vec3 L = normalize(sun_dir);
    vec3 Lh = normalize(vec3(sun_dir.x, 0, sun_dir.z));

    float vl = clamp((dot(V, L)-(1.0-sun_size)) / sun_size, 0.0, 1.0);

    vec4 Kc = clamp(vec4(sky_col + (col_shift * max(V.y, 0.0)), 1.0), 0.0, 1.0);
    vec4 Kg = vec4(sun_col, vl);

    float cA = clamp((dot(Lh, L)-0.5)/0.5, 0.0, 1.0);
    float xA = clamp(dot(Vh, Lh), 0.0, 1.0);
    float yA = clamp((dot(Vh, V)-0.8)/0.2, 0.0, 1.0);
    float hA = 0.2 - clamp(1.0 - (pow(xA/1.5, 2.0) + pow(yA/1.1, 2.0)), 0.0, 1.0);

    vec3 corona = vec3(1.0, 0.1, 0.1);

    Kg.rgb += corona * (cA+0.2);
    Kg.a += cA*hA;

    Kg = clamp(Kg, 0.0, 1.0);

    gl_FragColor = vec4(mix(Kc.rgb, Kg.rgb, Kg.a), 1.0);
}
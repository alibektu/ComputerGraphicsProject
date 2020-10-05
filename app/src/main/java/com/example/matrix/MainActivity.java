package com.example.matrix;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;
import java.lang.Math;

import javax.microedition.khronos.egl.EGLConfig;


public class MainActivity extends GvrActivity implements GvrView.StereoRenderer,
        SensorEventListener,ZaxisListener {


    double[] gravity;
    double[] linear_acceleration;


    double Z_axis;
    double Y_axis;
    double X_axis;

    DatagramSocket gSocket;
    Receive rxThread;

    private SensorManager mSensorManager;
    private Sensor mSensor;


    private static boolean wifiConnected = false;   // state of WiFi
    private static boolean mobileConnected = false; // state of LTE/mobile

    private User MyState = new User();
    private Users uList = new Users();

    public void startServerConnection()
    {
        if (checkConnection() == true) {

            try {
                gSocket = new DatagramSocket(); // startServerConnection
            } catch (SocketException e) {
                e.printStackTrace();
            }

            sendRegisterRequest();

            rxThread = new Receive(gSocket, MyState, uList, this, mSensorManager, mSensor);
            rxThread.addListener(this);
            MyState = rxThread.getUser();
            new Thread(rxThread).start();
        } else {
            Toast.makeText(getApplicationContext(),"No WiFi Connection",Toast.LENGTH_LONG).show();
            MyState.isConnected = false;
        }
    }

    private boolean checkConnection() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            MyState.getLocalIpAddress();
            return true;
        } else {
            return false;
        }
    }


    private void sendRegisterRequest() {
        final CharSequence text;
        text = "VR_Device;New User;";
        MyState.send(gSocket, text.toString());
    }


    String[] bullet_obj;

    private int bullet_vertex_n = 0;
    private int bullet_texture_n = 0;
    private int bullet_face_n= 0 ;

    private float[] bullet_vertex;
    private float[] bullet_texture;
    private float[] bullet_face;

    private float[] b_f;

    protected float[] modelPosition;

    private static final String TAG = "Matrix";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;


    private final float[] lightPosInEyeSpace = new float[4];

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer doorVertices;
    private FloatBuffer doorVertices2;
    private FloatBuffer doorTexture;
    private FloatBuffer doorTexture2;

    private FloatBuffer matrixVertices;
    private FloatBuffer matrixTexture;

    private FloatBuffer bulletVertices;
    private FloatBuffer bulletColors;
    private FloatBuffer bulletNormals;

    private int floorProgram;
    private int doorProgram;
    private int matrixProgram;
    private int bulletProgram;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;
    private int floorFadeParam;

    private int doorPositionParam;
    private int doorModelParam;
    private int doorModelViewParam;
    private int doorModelViewProjectionParam;
    private int doorTextureParam;
    private int doorFadeParam;

    private int matrixPositionParam;
    private int matrixModelParam;
    private int matrixModelViewParam;
    private int matrixModelViewProjectionParam;
    private int matrixTextureParam;

    private int bulletPositionParam;
    private int bulletNormalParam;
    private int bulletColorParam;
    private int bulletModelParam;
    private int bulletModelViewParam;
    private int bulletModelViewProjectionParam;
    private int bulletLightPosParam;
    private int bulletFadeParam;


    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;
    private float[] eyedirection;

    private float[] bulletmodel;
    private float[] eyePosition;
    private float[] bulletPosition;


    private float[] tempPosition;
    private float[] headRotation;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private float floorDepth = 20f;

    private float[] open;
    private float[] mvp;
    private float[] mv;
    private float[] m;
    private float[] t1;
    private float[] t2;
    private float[] t3;
    private float[] t4;
    private float[] t5;
    private float[] t6;
    private float[] t7;

    private float angle = 0.0f;
    private float angle1 = 0.0f;
    private float angle2 = 0.0f;
    private float angle3 = 0.0f;
    private float angle4 = 0.0f;
    private int m_t = 0;
    private int a = 1;


    MediaPlayer audio1;
    MediaPlayer bullet_sound;
    MediaPlayer dead_sound;
    MediaPlayer door_open;
    MediaPlayer door_close;

    private int texture;
    private int texture2;
    private int texture_w_s;
    private int door1;
    private int door2;
    private int door3;
    private int door4;
    private int[] texture_m;


    private int scene_number = 0;
    private int music_number = 0;


    private float b = 0.0f;

    private int bull = 1;
    private int bpx = 0;
    private int bpz = 0;
    private float dx;
    private float dz;

    private boolean lookState = false;
    private boolean lookState1 = false;
    private boolean lookState2 = false;
    private boolean lookState3 = false;
    private boolean lookState4 = false;

    private float bullet_time = -5f;

    private int[] path;
    private int pn = 0;

    public void parse_obj_file() {

        int a   ,b,c,d,i,j,n,m;
        String s,s1;
        for (i = 0; i < bullet_obj.length; i++ )
        {
            s = bullet_obj[i];

            j = 0;

            bullet_vertex = new float[13284 * 3];

            bullet_texture = new float[14097 * 3];

            bullet_face = new float[13201 * 6];

            b_f = new float[13201 * 3];

            //vt -0.630968 0.383333 0
            if (s.indexOf("vt") >= 0)
            {
                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1 += s.charAt(j);
                    j++;
                }
                j++;

                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_texture[bullet_texture_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_texture[bullet_texture_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( j < s.length() && s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_texture[bullet_texture_n++] = Float.parseFloat(s1);

            }

            //v 0 5.287255 0
            else if (s.indexOf("v") >= 0)
            {
                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1 += s.charAt(j);
                    j++;
                }
                j++;

                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_vertex[bullet_vertex_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_vertex[bullet_vertex_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( j < s.length() && s.charAt(j) != ' ' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_vertex[bullet_vertex_n++] = Float.parseFloat(s1);
            }

            //f 2348/2636 2349/2637 2227/2434 2226/2432
            else if (s.indexOf("f") >= 0 && s.charAt(j) != '/' )
            {
                s1 = "";
                while ( s.charAt(j) != ' ' )
                {
                    s1 += s.charAt(j);
                    j++;
                }
                j++;

                //////first
                s1 = "";
                while ( s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

                //////second

                s1 = "";
                while ( s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

                //////third

                s1 = "";
                while ( s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

                s1 = "";
                while ( j < s.length() && s.charAt(j) != ' ' && s.charAt(j) != '/' )
                {
                    s1+=s.charAt(j);
                    j++;
                }
                j++;

                bullet_face[bullet_face_n++] = Float.parseFloat(s1);

            }

        }


        for ( int hi = 0; hi < 13201 * 3; hi++  ){

            b_f[hi] = bullet_face[2 * hi + 1];
        }

    }

    public static int loadTexture( final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if( textureHandle[0] != 0 )
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        }

        if ( textureHandle[0] == 0 )
        {
            throw new RuntimeException("Error");
        }

        return textureHandle[0];
    }

    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        tempPosition = new float[4];
        // Model first appears directly in front of user.
        modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[4];
        headView = new float[16];

        mv = new float[16];
        mvp = new float[16];
        m = new float[16];
        open = new float[16];
        t1 = new float[16];
        t2 = new float[16];
        t3 = new float[16];
        t4 = new float[16];
        t5 = new float[16];
        t6 = new float[16];
        t7 = new float[16];

        eyedirection = new float[3];

        bulletmodel = new float[16];

        eyePosition = new float[3];
        bulletPosition = new float[3];

        texture_m = new int[30];

   //     bullet_obj = getResources().getStringArray(R.array.bullet_obj);
   //     parse_obj_file();

        eyePosition[0] = 0.0f;
        eyePosition[1] = 0.0f;
        eyePosition[2] = -CAMERA_Z;


        //New Added Variables
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = new double[3];
        linear_acceleration = new double[3];

        startServerConnection();

        path = new int[4];

        entrance_music();
    }

    public void entrance_music() {

        audio1 = MediaPlayer.create(this, R.raw.opening  );
        audio1.setLooping(true);
        audio1.start();
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_main);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);

        setGvrView(gvrView);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRendererShutdown() {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbFloorVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbFloorNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        floorColors = bbFloorColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);

        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, gridShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        floorFadeParam = GLES20.glGetUniformLocation(floorProgram, "i");

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");


        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.


        Matrix.setIdentityM(open, 0);
        Matrix.setIdentityM(mv, 0);
        Matrix.setIdentityM(t1, 0);
        Matrix.setIdentityM(t2, 0);
        Matrix.setIdentityM(t3, 0);
        Matrix.setIdentityM(t4, 0);
        Matrix.setIdentityM(t5, 0);
        Matrix.setIdentityM(t6, 0);
        Matrix.setIdentityM(t7, 0);
        Matrix.translateM(t1, 0, 10, 0, 37);
        Matrix.translateM(t2, 0, -10, 0, -37);
        Matrix.translateM(t3, 0, -10, 0, -80);
        Matrix.translateM(t4, 0, 0, 0, -42.5f);
        Matrix.translateM(t5, 0, -10, 0, 80);
        Matrix.rotateM(t6, 0, 90, 0, 1, 0);
        Matrix.rotateM(t7, 0, 180, 0, 1, 0);
        Matrix.rotateM(open, 0, angle, 0, 1, 0);

        //make a door
        ByteBuffer bbDoorVertices = ByteBuffer.allocateDirect(WorldLayoutData.DOOR_COORDS.length * 4);
        bbDoorVertices.order(ByteOrder.nativeOrder());
        doorVertices = bbDoorVertices.asFloatBuffer();
        doorVertices.put(WorldLayoutData.DOOR_COORDS);
        doorVertices.position(0);

        ByteBuffer bbDoorVertices2 = ByteBuffer.allocateDirect(WorldLayoutData.DOOR_COORDS2.length * 4);
        bbDoorVertices2.order(ByteOrder.nativeOrder());
        doorVertices2 = bbDoorVertices2.asFloatBuffer();
        doorVertices2.put(WorldLayoutData.DOOR_COORDS2);
        doorVertices2.position(0);

        ByteBuffer bbDoorTexture = ByteBuffer.allocateDirect(WorldLayoutData.DOOR_TEXTURE.length * 4);
        bbDoorTexture.order(ByteOrder.nativeOrder());
        doorTexture = bbDoorTexture.asFloatBuffer();
        doorTexture.put(WorldLayoutData.DOOR_TEXTURE);
        doorTexture.position(0);

        ByteBuffer bbDoorTexture2 = ByteBuffer.allocateDirect(WorldLayoutData.DOOR_TEXTURE2.length * 4);
        bbDoorTexture2.order(ByteOrder.nativeOrder());
        doorTexture2 = bbDoorTexture2.asFloatBuffer();
        doorTexture2.put(WorldLayoutData.DOOR_TEXTURE2);
        doorTexture2.position(0);

        vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.door_vertex);
        gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.door_fragment);

        doorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(doorProgram, vertexShader);
        GLES20.glAttachShader(doorProgram, gridShader);
        GLES20.glLinkProgram(doorProgram);
        GLES20.glUseProgram(doorProgram);

        doorModelParam = GLES20.glGetUniformLocation(doorProgram, "u_Model");
        doorModelViewParam = GLES20.glGetUniformLocation(doorProgram, "u_MVMatrix");
        doorModelViewProjectionParam = GLES20.glGetUniformLocation(doorProgram, "u_MVP");

        doorFadeParam = GLES20.glGetUniformLocation(doorProgram, "i");

        doorPositionParam = GLES20.glGetAttribLocation(doorProgram, "a_Position");
        doorTextureParam = GLES20.glGetAttribLocation(doorProgram, "a_Texture");


        //make matrix world
        ByteBuffer bbMatrixVertices = ByteBuffer.allocateDirect(WorldLayoutData.MATRIX_COORDS.length * 4);
        bbMatrixVertices.order(ByteOrder.nativeOrder());
        matrixVertices = bbMatrixVertices.asFloatBuffer();
        matrixVertices.put(WorldLayoutData.MATRIX_COORDS);
        matrixVertices.position(0);

        ByteBuffer bbMatrixTexture = ByteBuffer.allocateDirect(WorldLayoutData.MATRIX_TEXTURE.length * 4);
        bbMatrixTexture.order(ByteOrder.nativeOrder());
        matrixTexture = bbMatrixTexture.asFloatBuffer();
        matrixTexture.put(WorldLayoutData.MATRIX_TEXTURE);
        matrixTexture.position(0);

        vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.door_vertex);
        gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.door_fragment);

        matrixProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(matrixProgram, vertexShader);
        GLES20.glAttachShader(matrixProgram, gridShader);
        GLES20.glLinkProgram(matrixProgram);
        GLES20.glUseProgram(matrixProgram);

        matrixModelParam = GLES20.glGetUniformLocation(matrixProgram, "u_Model");
        matrixModelViewParam = GLES20.glGetUniformLocation(matrixProgram, "u_MVMatrix");
        matrixModelViewProjectionParam = GLES20.glGetUniformLocation(matrixProgram, "u_MVP");

        matrixPositionParam = GLES20.glGetAttribLocation(matrixProgram, "a_Position");
        matrixTextureParam = GLES20.glGetAttribLocation(matrixProgram, "a_Texture");


        //make bullet
        ByteBuffer bbBulletVertices = ByteBuffer.allocateDirect(WorldLayoutData.BULLET_COORDS.length * 4);
   //     ByteBuffer bbBulletVertices = ByteBuffer.allocateDirect( bullet_vertex.length * 4 );
        bbBulletVertices.order(ByteOrder.nativeOrder());
        bulletVertices = bbBulletVertices.asFloatBuffer();
        bulletVertices.put(WorldLayoutData.BULLET_COORDS);
   //     bulletVertices.put(bullet_vertex);
        bulletVertices.position(0);

        ByteBuffer bbBulletNormals = ByteBuffer.allocateDirect(WorldLayoutData.BULLET_NORMALS.length * 4);
        bbBulletNormals.order(ByteOrder.nativeOrder());
        bulletNormals = bbBulletNormals.asFloatBuffer();
        bulletNormals.put(WorldLayoutData.BULLET_NORMALS);
        bulletNormals.position(0);

        int bulletvShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.bullet_vertex);
        int bulletfShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.bullet_fragment);


        bulletProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(bulletProgram, bulletvShader);
        GLES20.glAttachShader(bulletProgram, bulletfShader);
        GLES20.glLinkProgram(bulletProgram);
        GLES20.glUseProgram(bulletProgram);

        bulletModelParam = GLES20.glGetUniformLocation(bulletProgram, "u_Model");
        bulletModelViewParam = GLES20.glGetUniformLocation(bulletProgram, "u_MVMatrix");
        bulletModelViewProjectionParam = GLES20.glGetUniformLocation(bulletProgram, "u_MVP");

        bulletPositionParam = GLES20.glGetAttribLocation(bulletProgram, "a_Position");
        bulletNormalParam = GLES20.glGetAttribLocation(bulletProgram, "a_Normal");

        bulletFadeParam = GLES20.glGetUniformLocation(bulletProgram, "i");




        texture = loadTexture(getBaseContext(), R.drawable.door);
        texture2 = loadTexture(getBaseContext(), R.drawable.wood);
        texture_w_s = loadTexture(getBaseContext(), R.drawable.woods);

        door1 = loadTexture(getBaseContext(), R.drawable.door1);
        door2 = loadTexture(getBaseContext(), R.drawable.door2);
        door3 = loadTexture(getBaseContext(), R.drawable.door3);
        door4 = loadTexture(getBaseContext(), R.drawable.door4);


        texture_m[0] = loadTexture(getBaseContext(), R.drawable.mat1);
        texture_m[1] = loadTexture(getBaseContext(), R.drawable.mat2);
        texture_m[2] = loadTexture(getBaseContext(), R.drawable.mat3);
        texture_m[3] = loadTexture(getBaseContext(), R.drawable.mat4);
        texture_m[4] = loadTexture(getBaseContext(), R.drawable.mat5);
        texture_m[5] = loadTexture(getBaseContext(), R.drawable.mat5);
        texture_m[6] = loadTexture(getBaseContext(), R.drawable.mat7);
        texture_m[7] = loadTexture(getBaseContext(), R.drawable.mat8);
        texture_m[8] = loadTexture(getBaseContext(), R.drawable.mat9);
        texture_m[9] = loadTexture(getBaseContext(), R.drawable.mat10);
        texture_m[10] = loadTexture(getBaseContext(), R.drawable.mat11);
        texture_m[11] = loadTexture(getBaseContext(), R.drawable.mat12);
        texture_m[12] = loadTexture(getBaseContext(), R.drawable.mat13);
        texture_m[13] = loadTexture(getBaseContext(), R.drawable.mat14);
        texture_m[14] = loadTexture(getBaseContext(), R.drawable.mat15);
        texture_m[15] = loadTexture(getBaseContext(), R.drawable.mat16);
        texture_m[16] = loadTexture(getBaseContext(), R.drawable.mat17);
        texture_m[17] = loadTexture(getBaseContext(), R.drawable.mat18);
        texture_m[18] = loadTexture(getBaseContext(), R.drawable.mat19);
        texture_m[19] = loadTexture(getBaseContext(), R.drawable.mat20);
        texture_m[20] = loadTexture(getBaseContext(), R.drawable.mat21);
        texture_m[21] = loadTexture(getBaseContext(), R.drawable.mat22);
        texture_m[22] = loadTexture(getBaseContext(), R.drawable.mat23);
        texture_m[23] = loadTexture(getBaseContext(), R.drawable.mat24);
        texture_m[24] = loadTexture(getBaseContext(), R.drawable.mat25);
        texture_m[25] = loadTexture(getBaseContext(), R.drawable.mat26);
        texture_m[26] = loadTexture(getBaseContext(), R.drawable.mat27);
        texture_m[27] = loadTexture(getBaseContext(), R.drawable.mat28);
        texture_m[28] = loadTexture(getBaseContext(), R.drawable.mat29);
        texture_m[29] = loadTexture(getBaseContext(), R.drawable.mat30);
    }

    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.

  /*      if ( scene_number == 0 ) {
            eye_update();

            Matrix.setLookAtM(camera, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    eyePosition[0], eyePosition[1], eyePosition[2] + 0.01f, 0.0f, 1.0f, 0.0f);
            headTransform.getHeadView(headView, 0);
            headTransform.getForwardVector(eyedirection, 0);
        }
        else if ( scene_number == 1 ){
            eye_update();
*/
            Matrix.setLookAtM(camera, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    eyePosition[0], eyePosition[1], eyePosition[2] + 0.01f, 0.0f, 1.0f, 0.0f);
            headTransform.getHeadView(headView, 0);
            headTransform.getForwardVector(eyedirection, 0);
 //       }
    }

    public void eye_update()
    {
        //eyePosition[0] = 0.0f;
        //eyePosition[1] = 0.0f;
        //eyePosition[2] = -CAMERA_Z;
    }


    public void door_animation() {
        GLES20.glActiveTexture(0);

        if (m_t % 600 >= 0 && m_t % 600 < 20 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[0]);
        else if (m_t % 600 >= 20 && m_t % 600 < 40 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[1]);
        else if (m_t % 600 >= 40 && m_t % 600 < 60 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[2]);
        else if (m_t % 600 >= 60 && m_t % 600 < 80 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[3]);
        else if (m_t % 600 >= 80 && m_t % 600 < 100 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[4]);
        else if (m_t % 600 >= 100 && m_t % 600 < 120 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[5]);
        else if (m_t % 600 >= 120 && m_t % 600 < 140 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[6]);
        else if (m_t % 600 >= 140 && m_t % 600 < 160 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[7]);
        else if (m_t % 600 >= 160 && m_t % 600 < 180 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[8]);
        else if (m_t % 600 >= 180 && m_t % 600 < 200 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[9]);
        else if (m_t % 600 >= 200 && m_t % 600 < 220 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[10]);
        else if (m_t % 600 >= 220 && m_t % 600 < 240 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[11]);
        else if (m_t % 600 >= 240 && m_t % 600 < 260 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[12]);
        else if (m_t % 600 >= 260 && m_t % 600 < 280 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[13]);
        else if (m_t % 600 >= 280 && m_t % 600 < 300 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[14]);
        else if (m_t % 600 >= 300 && m_t % 600 < 320 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[15]);
        else if (m_t % 600 >= 320 && m_t % 600 < 340 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[16]);
        else if (m_t % 600 >= 340 && m_t % 600 < 360 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[17]);
        else if (m_t % 600 >= 360 && m_t % 600 < 380 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[18]);
        else if (m_t % 600 >= 380 && m_t % 600 < 400 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[19]);
        else if (m_t % 600 >= 400 && m_t % 600 < 420 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[20]);
        else if (m_t % 600 >= 420 && m_t % 600 < 440 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[21]);
        else if (m_t % 600 >= 440 && m_t % 600 < 460 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[22]);
        else if (m_t % 600 >= 460 && m_t % 600 < 480 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[23]);
        else if (m_t % 600 >= 480 && m_t % 600 < 500 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[24]);
        else if (m_t % 600 >= 500 && m_t % 600 < 520 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[25]);
        else if (m_t % 600 >= 520 && m_t % 600 < 540 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[26]);
        else if (m_t % 600 >= 540 && m_t % 600 < 560 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[27]);
        else if (m_t % 600 >= 560 && m_t % 600 < 580 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[28]);
        else if (m_t % 600 >= 580 && m_t % 600 < 600 )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_m[29]);

        if ( m_t < 600 )
            m_t += a;

        if ( m_t == 600 || m_t == 0 ) {
            a *= -1;
            m_t += a;
        }
    }



    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if ( scene_number == 0 ) {

            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if ( b < 1.0)
                b += 0.003;

            // Apply the eye transformation to the camera.
            Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

            // Set the position of the light
            Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

            // Build the ModelView and ModelViewProjection matrices
            // for calculating cube position and light.
            float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

            // Set modelView for the floor, so we draw floor in the correct location
            Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
            drawFloor();

            Matrix.setIdentityM(open, 0);
            Matrix.rotateM(open, 0, angle, 0, 1, 0);

            Matrix.multiplyMM(m, 0, view, 0, modelFloor, 0);
            Matrix.multiplyMM(mv, 0, m, 0, t2, 0);
            Matrix.multiplyMM(m, 0, mv, 0, open, 0);
            Matrix.multiplyMM(modelView, 0, m, 0, t1, 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

            if (isLookingAtObjecto() == true && (angle <= 1.5 && angle > -135)) {
                angle -= 0.2;
            } else if (isLookingAtObjecto() == false) {

                if (angle < 0)
                    angle += 1;
            }

            GLES20.glActiveTexture(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

            drawDoor();

            Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
            GLES20.glActiveTexture(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2);

            drawDoor2();

            door_animation();

            matrixWorld();

            if ( angle < -90 && isLookingAtObjecto() && eyePosition[2] >= -37.5  && eyePosition[2] <= -36.5 ){

                scene_number = 1;
                music_number = 1;
                b = 0;

                eyePosition[0] = 0.0f;
                eyePosition[1] = 0.0f;
                eyePosition[2] = -CAMERA_Z;
            }


        }
        else if ( scene_number == 1  ) {
            GLES20.glClearColor(1f, 0.1f, 0.1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            makeRoom(eye);

        }
        else if ( scene_number == 2 ){
            GLES20.glClearColor(0.1f, 1f, 0.1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            makeRoom(eye);
        }
        else if ( scene_number == 3 ){
            GLES20.glClearColor(0.1f, 0.1f, 1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            makeRoom(eye);
        }
        else if ( scene_number == 4 ){
            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            makeRoom(eye);
        }
        else{
            GLES20.glClearColor(1.1f, 1.1f, 0.1f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            makeRoom(eye);
        }




    }


    @Override
    public void onFinishFrame(Viewport viewport) {
    }


    public void makeRoom(Eye eye){

        if ( music_number == scene_number )
            audio1.stop();

        if ( b < 1.0)
            b += 0.003;

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawFloor();


        Matrix.setIdentityM(open, 0);
        Matrix.rotateM(open, 0, angle1, 0, 1, 0);

        Matrix.multiplyMM(m, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(mv, 0, m, 0, t3, 0);
        Matrix.multiplyMM(m, 0, mv, 0, open, 0);
        Matrix.multiplyMM(modelView, 0, m, 0, t1, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        if (isLookingAtObject1() == true && (angle1 <= 1.5 && angle1 > -135)) {
            angle1 -= 0.2;
        } else if (isLookingAtObject1() == false) {

            if (angle1 < 0)
                angle1 += 1;
        }

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        drawDoor();

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, door1);


        Matrix.multiplyMM(m, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelView, 0, m, 0, t4, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        matrixWorld();

        //----------------

        Matrix.setIdentityM(open, 0);
        Matrix.rotateM(open, 0, angle2, 0, 1, 0);

        Matrix.multiplyMM(m, 0, view, 0, t6, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(m, 0, mv, 0, t3, 0);
        Matrix.multiplyMM(mv, 0, m, 0, open, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t1, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);


        if (isLookingAtObject2() == true && (angle2 <= 1.5 && angle2 > -135)) {
            angle2 -= 0.2;
        } else if (isLookingAtObject2() == false) {

            if (angle2 < 0)
                angle2 += 1;
        }

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        drawDoor();

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, door2);

        Matrix.multiplyMM(m, 0, view, 0, t6, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t4, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        matrixWorld();
        //----------

        Matrix.setIdentityM(open, 0);
        Matrix.rotateM(open, 0, angle3, 0, 1, 0);
        Matrix.setIdentityM(t7, 0);
        Matrix.rotateM(t7, 0, 180, 0, 1, 0);

        Matrix.multiplyMM(m, 0, view, 0, t7, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(m, 0, mv, 0, t3, 0);
        Matrix.multiplyMM(mv, 0, m, 0, open, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t1, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        if (isLookingAtObject3() == true && (angle3 <= 1.5 && angle3 > -135)) {
            angle3 -= 0.2;
        } else if (isLookingAtObject3() == false) {

            if (angle3 < 0)
                angle3 += 1;
        }

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        drawDoor();

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, door3);

        Matrix.multiplyMM(m, 0, view, 0, t7, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t4, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        matrixWorld();

        //------

        Matrix.setIdentityM(open, 0);
        Matrix.rotateM(open, 0, angle4, 0, 1, 0);
        Matrix.setIdentityM(t7, 0);
        Matrix.rotateM(t7, 0, -90, 0, 1, 0);

        Matrix.multiplyMM(m, 0, view, 0, t7, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(m, 0, mv, 0, t3, 0);
        Matrix.multiplyMM(mv, 0, m, 0, open, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t1, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        if (isLookingAtObject4() == true && (angle4 <= 1.5 && angle4 > -135)) {
            angle4 -= 0.2;
        } else if (isLookingAtObject4() == false) {

            if (angle4 < 0)
                angle4 += 1;
        }

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        drawDoor();

        GLES20.glActiveTexture(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, door4);

        Matrix.multiplyMM(m, 0, view, 0, t7, 0);
        Matrix.multiplyMM(mv, 0, m, 0, modelFloor, 0);
        Matrix.multiplyMM(modelView, 0, mv, 0, t4, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        matrixWorld();

        if ( bullet_time < 1.0 )
            bullet_time += 0.01;

        //      shooting(eye);

        if ( isDead()  ){
            dead_sound = MediaPlayer.create(this, R.raw.alibek  );
            dead_sound.start();
            scene_number = 10;
        }

        nextScene();
    }

    public void nextScene(){

        int randomscene;

        if ( path[0] == 3 && path[1] == 2  )
            scene_number = 5;
        else {

            if (angle1 < -90 && isLookingAtObject1() && eyePosition[2] < -77.5 && eyePosition[2] > -80) {
                Random r = new Random();
                randomscene = r.nextInt(4) + 1;
                scene_number = randomscene;
                music_number = randomscene;
                angle1 = 0;
                b = 0;
                eyePosition[0] = 0.0f;
                eyePosition[1] = 0.0f;
                eyePosition[2] = -CAMERA_Z;
                path[pn % 2] = scene_number;
                pn++;
            }

            if (angle2 < -90 && isLookingAtObject2() && eyePosition[0] < -77.5 && eyePosition[0] > -80) {
                Random r = new Random();
                randomscene = r.nextInt(4) + 1;
                scene_number = randomscene;
                music_number = randomscene;
                angle2 = 0;
                b = 0;
                eyePosition[0] = 0.0f;
                eyePosition[1] = 0.0f;
                eyePosition[2] = -CAMERA_Z;
                path[pn % 2] = scene_number;
                pn++;
            }

            if (angle3 < -90 && isLookingAtObject3() && eyePosition[2] > 77.5 && eyePosition[2] < 80) {
                Random r = new Random();
                randomscene = r.nextInt(4) + 1;
                scene_number = randomscene;
                music_number = randomscene;
                angle3 = 0;
                b = 0;
                eyePosition[0] = 0.0f;
                eyePosition[1] = 0.0f;
                eyePosition[2] = -CAMERA_Z;
                path[pn % 2] = scene_number;
                pn++;
            }

            if (angle4 < -90 && isLookingAtObject4() && eyePosition[0] > 77.5 && eyePosition[0] < 80) {
                Random r = new Random();
                randomscene = r.nextInt(4) + 1;
                scene_number = randomscene;
                music_number = randomscene;
                angle4 = 0;
                b = 0;
                eyePosition[0] = 0.0f;
                eyePosition[1] = 0.0f;
                eyePosition[2] = -CAMERA_Z;
                path[pn % 2] = scene_number;
                pn++;
            }
        }
    }

    public void drawFloor() {
        GLES20.glUseProgram(floorProgram);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(
                floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        GLES20.glUniform1f(floorFadeParam, b );

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);
    }

    public void drawDoor() {

        GLES20.glUseProgram(doorProgram);

        GLES20.glUniformMatrix4fv(doorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(doorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(doorModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(doorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, doorVertices);
        GLES20.glVertexAttribPointer(doorTextureParam, 2, GLES20.GL_FLOAT, false, 0, doorTexture);

        GLES20.glEnableVertexAttribArray(doorPositionParam);
        GLES20.glEnableVertexAttribArray(doorTextureParam);

        GLES20.glUniform1f(doorFadeParam, b);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 30);
    }

    public void drawDoor2() {

        GLES20.glUseProgram(doorProgram);

        GLES20.glUniformMatrix4fv(doorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(doorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(doorModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(doorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, doorVertices2);
        GLES20.glVertexAttribPointer(doorTextureParam, 2, GLES20.GL_FLOAT, false, 0, doorTexture2);

        GLES20.glEnableVertexAttribArray(doorPositionParam);
        GLES20.glEnableVertexAttribArray(doorTextureParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);
    }

    public void matrixWorld(){
        GLES20.glUseProgram(matrixProgram);

        GLES20.glUniformMatrix4fv(matrixModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(matrixModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(matrixModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(matrixPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, matrixVertices);
        GLES20.glVertexAttribPointer(matrixTextureParam, 2, GLES20.GL_FLOAT, false, 0, matrixTexture);

        GLES20.glEnableVertexAttribArray(matrixPositionParam);
        GLES20.glEnableVertexAttribArray(matrixTextureParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    public void bullet(){
        GLES20.glUseProgram(bulletProgram);

        GLES20.glUniformMatrix4fv(bulletModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(bulletModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(bulletPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, bulletVertices);
        GLES20.glVertexAttribPointer(bulletNormalParam, 3, GLES20.GL_FLOAT, false, 0, bulletNormals);

        GLES20.glEnableVertexAttribArray(bulletPositionParam);
        GLES20.glEnableVertexAttribArray(bulletNormalParam);

        GLES20.glUniform1f(bulletFadeParam, b);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

   //     GLES20.glDrawElements( GLES20.GL_TRIANGLES, b_f.length, GLES20.GL_UNSIGNED_INT, 0 );
    }

    public void shooting(Eye eye){

        if ( bullet_time >= 1 ) {

            float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

            if (isBullet()) {
                Random r = new Random();
                bpx = r.nextInt(160);
                bpz = r.nextInt(160-bpx);
                int d = r.nextInt(10);

                if (d % 2 == 0)
                    bpx *= -1;

                d = r.nextInt(10);
                if (d % 2 == 0)
                    bpz *= -1;

                bullet_sound = MediaPlayer.create(this, R.raw.bullet);
                bullet_sound.start();

                bulletPosition[0] = bpx;
                bulletPosition[1] = 0.0f;
                bulletPosition[2] = bpz;

                dx = eyePosition[0] - bulletPosition[0];
                dz = eyePosition[2] - bulletPosition[2];
            }


            if ( Math.abs(bulletPosition[0]) <= 160 && Math.abs(bulletPosition[2])  <= 160 ) {

                bulletPosition[0] += dx * 0.005;
                bulletPosition[2] += dz * 0.005;

                bull = 0;
            }
            else {
                bullet_time -= 1;
                bull = 1;
            }

            Matrix.setIdentityM(bulletmodel, 0);
            Matrix.translateM(bulletmodel, 0, bulletPosition[0], bulletPosition[1], bulletPosition[2]);

            GLES20.glUseProgram(bulletProgram);
            Matrix.multiplyMM(modelView, 0, view, 0, bulletmodel, 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

            bullet();
        }
    }

    public boolean isBullet() {
        if ( bull == 0 )
            return false;
        else
            return true;
    }

    private boolean isLookingAtObjecto() {

        if ( eyedirection[2] > 0.85 && eyedirection[2] < 1  && eyePosition[2] < -10 && eyePosition[2] > -40 ) {

            if ( !lookState ){
                door_open = MediaPlayer.create(this, R.raw.door_open );
                door_open.start();

                lookState = true;
            }


            return true;
        }
        else {
            if (lookState && angle > -30) {
                door_close = MediaPlayer.create(this, R.raw.door_close);
                door_close.start();

                lookState = false;
            }

            return false;
        }
    }

    private boolean isLookingAtObject1() {

        if ( eyedirection[2] > 0.85 && eyedirection[2] < 1  && eyePosition[2] < -50 && eyePosition[2] > -80 ) {

            if ( !lookState1 ){
                door_open = MediaPlayer.create(this, R.raw.door_open );
                door_open.start();

                lookState1 = true;
            }


            return true;
        }
        else {
            if (lookState1 && angle1 > -30) {
                door_close = MediaPlayer.create(this, R.raw.door_close);
                door_close.start();

                lookState1 = false;
            }

            return false;
        }
    }

    private boolean isLookingAtObject2() {

        if ( eyedirection[0] > 0.85 && eyedirection[0] < 1  && eyePosition[0] < -50 && eyePosition[0] > -80 ) {

            if ( !lookState2 ){
                door_open = MediaPlayer.create(this, R.raw.door_open );
                door_open.start();

                lookState2 = true;
            }


            return true;
        }
        else {
            if (lookState2 && angle2 > -30) {
                door_close = MediaPlayer.create(this, R.raw.door_close);
                door_close.start();

                lookState2 = false;
            }

            return false;
        }
    }

    private boolean isLookingAtObject3() {

        if ( eyedirection[2] < -0.85 && eyedirection[2] > -1 && eyePosition[2] > 50 && eyePosition[2] < 80   ) {

            if ( !lookState3 ){
                door_open = MediaPlayer.create(this, R.raw.door_open );
                door_open.start();

                lookState3 = true;
            }


            return true;
        }
        else {
            if (lookState3 && angle3 > -30) {
                door_close = MediaPlayer.create(this, R.raw.door_close);
                door_close.start();

                lookState3 = false;
            }

            return false;
        }
    }

    private boolean isLookingAtObject4() {

        if ( eyedirection[0] < -0.85 && eyedirection[0] > -1 && eyePosition[0] > 50 && eyePosition[0] < 80   ) {

            if ( !lookState4 ){
                door_open = MediaPlayer.create(this, R.raw.door_open );
                door_open.start();

                lookState4 = true;
            }


            return true;
        }
        else {
            if (lookState4 && angle4 > -30) {
                door_close = MediaPlayer.create(this, R.raw.door_close);
                door_close.start();

                lookState4 = false;
            }

            return false;
        }
    }


    public boolean isDead(){

        if ( Math.abs(eyePosition[0]-bulletPosition[0]) < 1 && Math.abs(eyePosition[1]-bulletPosition[1]) < 1 &&
                Math.abs(eyePosition[2]-bulletPosition[2]) < 1 && bull == 0 )
            return true;
        else
            return false;
    }


    @Override
    public void onCardboardTrigger() {
/*
        scene_number = 1;
        music_number = 1;

        b = 0.0f;
        bull = 1;

        */

        eyePosition[0] -= eyedirection[0] * 1;
    //    eyePosition[1] -= eyedirection[1] * 1;
        eyePosition[2] -= eyedirection[2] * 1;


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.



        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void ReceivedFromSensorZ()
    {
        eyePosition[2] -= eyedirection[2] * 4;
    }

    @Override
    public void ReceivedFromSensorX_R()
    {
        eyePosition[0] -= eyedirection[0] * 4;
    }

    @Override
    public void ReceivedFromSensorX_L()
    {
        eyePosition[0] += eyedirection[0] * 4;
    }

}

package com.group_finity.mascot.sound;

import com.jogamp.openal.sound3d.Vec3f;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 13-1-17
 * Time: 下午4:13
 */
public class SoundSource {
    private final int sourceID;
    private Sound Sound;

    SoundSource(){
        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGenSources(1, arrayOfInt, 0);
        sourceID = arrayOfInt[0];
    }

    public void play()
    {
        SoundFactory.al.alSourcePlay(this.sourceID);
    }

    public void pause()
    {
        SoundFactory.al.alSourcePause(this.sourceID);
    }

    public void stop()
    {
        SoundFactory.al.alSourceStop(this.sourceID);
    }

    public void rewind()
    {
        SoundFactory.al.alSourceRewind(this.sourceID);
    }

    public void delete()
    {
        SoundFactory.al.alDeleteSources(1, new int[] { this.sourceID }, 0);
    }

    public void setPitch(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4099, paramFloat);
    }

    public float getPitch()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4099, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setGain(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4106, paramFloat);
    }

    public float getGain()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4106, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setMaxDistance(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4131, paramFloat);
    }

    public float getMaxDistance()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4131, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setRolloffFactor(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4129, paramFloat);
    }

    public float getRolloffFactor()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4129, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setReferenceDistance(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4128, paramFloat);
    }

    public float getReferenceDistance()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4128, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setMinGain(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4109, paramFloat);
    }

    public float getMinGain()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4109, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setMaxGain(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4110, paramFloat);
    }

    public float getMaxGain()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4110, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setConeOuterGain(float paramFloat)
    {
        SoundFactory.al.alSourcef(this.sourceID, 4130, paramFloat);
    }

    public float getConeOuterGain()
    {
        float[] arrayOfFloat = new float[1];
        SoundFactory.al.alGetSourcef(this.sourceID, 4130, arrayOfFloat, 0);

        return arrayOfFloat[0];
    }

    public void setPosition(Vec3f paramVec3f)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4100, paramVec3f.v1, paramVec3f.v2, paramVec3f.v3);
    }

    public void setPosition(float paramFloat1, float paramFloat2, float paramFloat3)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4100, paramFloat1, paramFloat2, paramFloat3);
    }

    public Vec3f getPosition()
    {
        Vec3f localVec3f = null;
        float[] arrayOfFloat = new float[3];
        SoundFactory.al.alGetSourcefv(this.sourceID, 4100, arrayOfFloat, 0);
        localVec3f = new Vec3f(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2]);

        return localVec3f;
    }

    public void setVelocity(Vec3f paramVec3f)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4102, paramVec3f.v1, paramVec3f.v2, paramVec3f.v3);
    }

    public void setVelocity(float paramFloat1, float paramFloat2, float paramFloat3)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4102, paramFloat1, paramFloat2, paramFloat3);
    }

    public Vec3f getVelocity()
    {
        Vec3f localVec3f = null;
        float[] arrayOfFloat = new float[3];
        SoundFactory.al.alGetSourcefv(this.sourceID, 4102, arrayOfFloat, 0);
        localVec3f = new Vec3f(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2]);

        return localVec3f;
    }

    public void setDirection(Vec3f paramVec3f)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4101, paramVec3f.v1, paramVec3f.v2, paramVec3f.v3);
    }

    public void setDirection(float paramFloat1, float paramFloat2, float paramFloat3)
    {
        SoundFactory.al.alSource3f(this.sourceID, 4101, paramFloat1, paramFloat2, paramFloat3);
    }

    public Vec3f getDirection()
    {
        Vec3f localVec3f = null;
        float[] arrayOfFloat = new float[3];
        SoundFactory.al.alGetSourcefv(this.sourceID, 4101, arrayOfFloat, 0);
        localVec3f = new Vec3f(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2]);

        return localVec3f;
    }

    public void setSourceRelative(boolean paramBoolean)
    {
        int i = paramBoolean ? 1 : 0;
        SoundFactory.al.alSourcei(this.sourceID, 514, i);
    }

    public boolean isSourceRelative()
    {
        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGetSourcei(this.sourceID, 514, arrayOfInt, 0);

        return arrayOfInt[0] == 1;
    }

    public void setLooping(boolean paramBoolean)
    {
        int i = paramBoolean ? 1 : 0;
        SoundFactory.al.alSourcei(this.sourceID, 4103, i);
    }

    public boolean getLooping()
    {
        int i = 0;
        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGetSourcei(this.sourceID, 4103, arrayOfInt, 0);
        return arrayOfInt[0] == 1;
    }

    public int getBuffersQueued()
    {
        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGetSourcei(this.sourceID, 4117, arrayOfInt, 0);

        return arrayOfInt[0];
    }

    public int getBuffersProcessed()
    {
        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGetSourcei(this.sourceID, 4118, arrayOfInt, 0);

        return arrayOfInt[0];
    }

    public void setBuffer(Sound paramBuffer)
    {
        SoundFactory.al.alSourcei(this.sourceID, 4105, paramBuffer.bufferID);
        this.Sound = paramBuffer;
    }

    public Sound getBuffer()
    {
        return this.Sound;
    }

    public void queueBuffers(Sound[] paramArrayOfBuffer)
    {
        int i = paramArrayOfBuffer.length;
        int[] arrayOfInt = new int[i];

        for (int j = 0; j < i; j++) {
            arrayOfInt[j] = paramArrayOfBuffer[j].bufferID;
        }

        SoundFactory.al.alSourceQueueBuffers(this.sourceID, i, arrayOfInt, 0);
    }

    public void unqueueBuffers(Sound[] paramArrayOfBuffer)
    {
        int i = paramArrayOfBuffer.length;
        int[] arrayOfInt = new int[i];

        for (int j = 0; j < i; j++) {
            arrayOfInt[j] = paramArrayOfBuffer[j].bufferID;
        }

        SoundFactory.al.alSourceUnqueueBuffers(this.sourceID, i, arrayOfInt, 0);
    }
}

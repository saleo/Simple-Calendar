/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/leo/work/skcal/davdroid/src/main/aidl/at/bitfire/davdroid/settings/ISettingsObserver.aidl
 */
package at.bitfire.davdroid.settings;
public interface ISettingsObserver extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements at.bitfire.davdroid.settings.ISettingsObserver
{
private static final java.lang.String DESCRIPTOR = "at.bitfire.davdroid.settings.ISettingsObserver";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an at.bitfire.davdroid.settings.ISettingsObserver interface,
 * generating a proxy if needed.
 */
public static at.bitfire.davdroid.settings.ISettingsObserver asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof at.bitfire.davdroid.settings.ISettingsObserver))) {
return ((at.bitfire.davdroid.settings.ISettingsObserver)iin);
}
return new at.bitfire.davdroid.settings.ISettingsObserver.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onSettingsChanged:
{
data.enforceInterface(DESCRIPTOR);
this.onSettingsChanged();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements at.bitfire.davdroid.settings.ISettingsObserver
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onSettingsChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSettingsChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSettingsChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onSettingsChanged() throws android.os.RemoteException;
}

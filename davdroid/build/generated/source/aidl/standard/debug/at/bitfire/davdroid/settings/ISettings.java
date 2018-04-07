/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/leo/work/skcal/davdroid/src/main/aidl/at/bitfire/davdroid/settings/ISettings.aidl
 */
package at.bitfire.davdroid.settings;
public interface ISettings extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements at.bitfire.davdroid.settings.ISettings
{
private static final java.lang.String DESCRIPTOR = "at.bitfire.davdroid.settings.ISettings";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an at.bitfire.davdroid.settings.ISettings interface,
 * generating a proxy if needed.
 */
public static at.bitfire.davdroid.settings.ISettings asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof at.bitfire.davdroid.settings.ISettings))) {
return ((at.bitfire.davdroid.settings.ISettings)iin);
}
return new at.bitfire.davdroid.settings.ISettings.Stub.Proxy(obj);
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
case TRANSACTION_forceReload:
{
data.enforceInterface(DESCRIPTOR);
this.forceReload();
reply.writeNoException();
return true;
}
case TRANSACTION_has:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.has(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getBoolean:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.getBoolean(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getInt:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _result = this.getInt(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getLong:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
long _result = this.getLong(_arg0, _arg1);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getString:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.getString(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_isWritable:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isWritable(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_putBoolean:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.putBoolean(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_putInt:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.putInt(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_putLong:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
boolean _result = this.putLong(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_putString:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.putString(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_remove:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.remove(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_registerObserver:
{
data.enforceInterface(DESCRIPTOR);
at.bitfire.davdroid.settings.ISettingsObserver _arg0;
_arg0 = at.bitfire.davdroid.settings.ISettingsObserver.Stub.asInterface(data.readStrongBinder());
this.registerObserver(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterObserver:
{
data.enforceInterface(DESCRIPTOR);
at.bitfire.davdroid.settings.ISettingsObserver _arg0;
_arg0 = at.bitfire.davdroid.settings.ISettingsObserver.Stub.asInterface(data.readStrongBinder());
this.unregisterObserver(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements at.bitfire.davdroid.settings.ISettings
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
@Override public void forceReload() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_forceReload, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean has(java.lang.String key) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
mRemote.transact(Stub.TRANSACTION_has, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getBoolean(java.lang.String key, boolean defaultValue) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeInt(((defaultValue)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_getBoolean, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getInt(java.lang.String key, int defaultValue) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeInt(defaultValue);
mRemote.transact(Stub.TRANSACTION_getInt, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public long getLong(java.lang.String key, long defaultValue) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeLong(defaultValue);
mRemote.transact(Stub.TRANSACTION_getLong, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getString(java.lang.String key, java.lang.String defaultValue) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeString(defaultValue);
mRemote.transact(Stub.TRANSACTION_getString, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isWritable(java.lang.String key) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
mRemote.transact(Stub.TRANSACTION_isWritable, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean putBoolean(java.lang.String key, boolean value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeInt(((value)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_putBoolean, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean putInt(java.lang.String key, int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_putInt, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean putLong(java.lang.String key, long value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeLong(value);
mRemote.transact(Stub.TRANSACTION_putLong, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean putString(java.lang.String key, java.lang.String value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeString(value);
mRemote.transact(Stub.TRANSACTION_putString, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean remove(java.lang.String key) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
mRemote.transact(Stub.TRANSACTION_remove, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void registerObserver(at.bitfire.davdroid.settings.ISettingsObserver observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerObserver, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterObserver(at.bitfire.davdroid.settings.ISettingsObserver observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterObserver, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_forceReload = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_has = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getBoolean = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getInt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getLong = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isWritable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_putBoolean = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_putInt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_putLong = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_putString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_remove = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_registerObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_unregisterObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
}
public void forceReload() throws android.os.RemoteException;
public boolean has(java.lang.String key) throws android.os.RemoteException;
public boolean getBoolean(java.lang.String key, boolean defaultValue) throws android.os.RemoteException;
public int getInt(java.lang.String key, int defaultValue) throws android.os.RemoteException;
public long getLong(java.lang.String key, long defaultValue) throws android.os.RemoteException;
public java.lang.String getString(java.lang.String key, java.lang.String defaultValue) throws android.os.RemoteException;
public boolean isWritable(java.lang.String key) throws android.os.RemoteException;
public boolean putBoolean(java.lang.String key, boolean value) throws android.os.RemoteException;
public boolean putInt(java.lang.String key, int value) throws android.os.RemoteException;
public boolean putLong(java.lang.String key, long value) throws android.os.RemoteException;
public boolean putString(java.lang.String key, java.lang.String value) throws android.os.RemoteException;
public boolean remove(java.lang.String key) throws android.os.RemoteException;
public void registerObserver(at.bitfire.davdroid.settings.ISettingsObserver observer) throws android.os.RemoteException;
public void unregisterObserver(at.bitfire.davdroid.settings.ISettingsObserver observer) throws android.os.RemoteException;
}

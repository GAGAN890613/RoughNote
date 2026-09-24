package com.gagan.roughnotes

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import kotlin.math.min

class OverlayService: Service() {
 private lateinit var wm:WindowManager
 private var bubble:TextView?=null
 private var panel:LinearLayout?=null
 private var pad:Pad?=null
 private var params:WindowManager.LayoutParams?=null
 private var full=false; private var nano=false; private var alpha=0x30
 override fun onBind(i:Intent?):IBinder?=null
 override fun onStartCommand(i:Intent?,f:Int,id:Int):Int {
  if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY}
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  if(bubble==null) makeBubble()
  return START_STICKY
 }
 private fun lp(w:Int,h:Int)=WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT)
 private fun dp(n:Int)=(n*resources.displayMetrics.density+.5f).toInt()
 private fun makeBubble(){
  val b=TextView(this).apply{text="✎";textSize=25f;gravity=Gravity.CENTER;setTextColor(-1);background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(0xff2563eb.toInt())}}
  val p=lp(dp(54),dp(54)).apply{gravity=Gravity.TOP or Gravity.END;x=dp(8);y=dp(180)}
  b.setOnTouchListener(object:View.OnTouchListener{
   var x=0f;var y=0f;var ox=0;var oy=0;var moved=false
   override fun onTouch(v:View,e:MotionEvent):Boolean{when(e.action){
    MotionEvent.ACTION_DOWN->{x=e.rawX;y=e.rawY;ox=p.x;oy=p.y;moved=false;return true}
    MotionEvent.ACTION_MOVE->{val dx=(e.rawX-x).toInt();val dy=(e.rawY-y).toInt();if(kotlin.math.abs(dx)+kotlin.math.abs(dy)>dp(6))moved=true;p.x=(ox-dx).coerceAtLeast(0);p.y=(oy+dy).coerceAtLeast(0);wm.updateViewLayout(b,p);return true}
    MotionEvent.ACTION_UP->{if(!moved)if(panel==null)open()else close();return true}
   };return true}
  })
  bubble=b;wm.addView(b,p)
 }
 private fun open(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(7),dp(5),dp(7),dp(7));background=GradientDrawable().apply{cornerRadius=dp(14).toFloat();setColor((alpha shl 24) or 0x0010182b);setStroke(dp(1),0x8860a5fa.toInt())}}
  val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
  header.addView(TextView(this).apply{text="ROUGHNOTES";textSize=14f;setTextColor(-1)},LinearLayout.LayoutParams(0,dp(42),1f))
  fun button(s:String,fn:()->Unit)=TextView(this).apply{text=s;textSize=12f;gravity=Gravity.CENTER;setTextColor(-1);setPadding(dp(8),dp(6),dp(8),dp(6));setOnClickListener{fn()}}
  val nanoBtn=button("Nano"){nano=!nano;full=false;resize()}
  val fullBtn=button("Full"){full=!full;nano=false;resize()}
  header.addView(nanoBtn);header.addView(fullBtn);header.addView(button("×"){close()});root.addView(header)
  val tools=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  listOf("Pen","Line","Oval","Undo","Clear").forEachIndexed{idx,s->tools.addView(button(s){pad?.mode=idx.coerceAtMost(2);if(idx==3)pad?.undo();if(idx==4)pad?.clear()})}
  root.addView(tools)
  val colors=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER}
  listOf(0xfff8fafc.toInt(),0xff111827.toInt(),0xffef4444.toInt(),0xff3b82f6.toInt(),0xff22c55e.toInt(),0xfff59e0b.toInt(),0xffa855f7.toInt()).forEach{c->addView(button("●"){pad?.ink=c}.apply{setTextColor(c);textSize=21f})}
  colors.addView(button("Glass"){alpha=if(alpha==0x30)0x18 else if(alpha==0x18)0x60 else 0x30;root.background=GradientDrawable().apply{cornerRadius=dp(14).toFloat();setColor((alpha shl 24) or 0x0010182b);setStroke(dp(1),0x8860a5fa.toInt())}})
  root.addView(HorizontalScrollView(this).apply{isHorizontalScrollBarEnabled=false;addView(colors)})
  pad=Pad(this);root.addView(pad,LinearLayout.LayoutParams(-1,0,1f))
  params=lp(dp(340),dp(420)).apply{gravity=Gravity.CENTER};panel=root;wm.addView(root,params);resize()
 }
 private fun resize(){val p=panel?:return;val q=params?:return;val w=resources.displayMetrics.widthPixels;val h=resources.displayMetrics.heightPixels;q.width=when{full->-1;nano->min(dp(280),w-dp(16));else->min((w*.96).toInt(),dp(680))};q.height=when{full->-1;nano->dp(115);else->min((h*.72).toInt(),dp(650))};q.gravity=if(full)Gravity.TOP or Gravity.START else Gravity.CENTER;wm.updateViewLayout(p,q)}
 private fun close(){panel?.let{wm.removeView(it)};panel=null;pad=null;params=null}
 override fun onDestroy(){close();bubble?.let{wm.removeView(it)};bubble=null;super.onDestroy()}
}
private class Pad(c:android.content.Context):View(c){
 private data class Stroke(val path:Path,val color:Int,val mode:Int,val rect:RectF?)
 private val strokes=mutableListOf<Stroke>();private var path:Path?=null;private var sx=0f;private var sy=0f;var mode=0;var ink=0xfff8fafc.toInt()
 private val paint=Paint(3).apply{style=Paint.Style.STROKE;strokeWidth=4f;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND}
 override fun onDraw(c:Canvas){strokes.forEach{paint.color=it.color;if(it.mode==2&&it.rect!=null)c.drawOval(it.rect,paint)else c.drawPath(it.path,paint)};path?.let{paint.color=ink;c.drawPath(it,paint)}}
 override fun onTouchEvent(e:MotionEvent):Boolean{when(e.action){MotionEvent.ACTION_DOWN->{sx=e.x;sy=e.y;path=Path().apply{moveTo(sx,sy)};invalidate();return true};MotionEvent.ACTION_MOVE->{if(mode==0)path?.lineTo(e.x,e.y);invalidate();return true};MotionEvent.ACTION_UP->{val p=path?:Path();if(mode==1){p.reset();p.moveTo(sx,sy);p.lineTo(e.x,e.y)};val r=if(mode==2)RectF(sx,sy,e.x,e.y).apply{sort()}else null;strokes.add(Stroke(p,ink,mode,r));path=null;invalidate();return true}};return true}
 fun undo(){if(strokes.isNotEmpty())strokes.removeAt(strokes.lastIndex);invalidate()}
 fun clear(){strokes.clear();invalidate()}
}

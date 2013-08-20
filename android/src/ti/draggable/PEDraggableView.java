/**
 *   Copyright 2012 Pedro Enrique
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package ti.draggable;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class PEDraggableView extends TiUIView {
	protected TiCompositeLayout.LayoutParams layout;
	protected ViewProxy proxy;
	protected OnTouchListener listener;

	public class TiCompositeLayoutInterceptor extends TiCompositeLayout {
		public TiCompositeLayoutInterceptor(Context context, LayoutArrangement arrangement) {
			super(context, arrangement);
		}

		@Override
	    public boolean onInterceptTouchEvent(MotionEvent event) {
			if (listener != null) {
				return listener.onTouch(this, event);
			}

			return false;
	    }
	}

	public PEDraggableView(ViewProxy invokingProxy, TiViewProxy createdProxy) {
		super(createdProxy);

		proxy = invokingProxy;

		LayoutArrangement arrangement = LayoutArrangement.DEFAULT;

		if (proxy.hasProperty(TiC.PROPERTY_LAYOUT)) {
			String layoutProperty = TiConvert.toString(proxy.getProperty(TiC.PROPERTY_LAYOUT));

			if (layoutProperty.equals(TiC.LAYOUT_HORIZONTAL)) {
				arrangement = LayoutArrangement.HORIZONTAL;
			} else if (layoutProperty.equals(TiC.LAYOUT_VERTICAL)) {
				arrangement = LayoutArrangement.VERTICAL;
			}
		}

		TiCompositeLayout view = new TiCompositeLayoutInterceptor(proxy.getActivity(), arrangement);

		view.setOnTouchListener(listener = new OnTouchListener() {
			private int threshold = 10;
			private KrollDict props = new KrollDict();
			private KrollDict center = new KrollDict();
			private String directionVertical = "neutral";
			private String directionHorizontal = "neutral";
			private boolean hasListenerStart = false;
			private boolean hasListenerMove = false;
			private boolean hasListenerEnd = false;
			private boolean isDragging = false;
			private float positionLeft = 0;
			private float positionTop = 0;
			private float currentTop = 0;
			private float currentLeft = 0;
			private float lastTop = 0;
			private float lastLeft = 0;
			private float startTop = 0;
			private float startLeft = 0;
			private float startX = 0;
			private float startY = 0;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				boolean capture = false;

				if (proxy.getIsDraggable()) {
					if ((view.getLayoutParams() instanceof TiCompositeLayout.LayoutParams) == false) {
						view = (ViewGroup) view.getParent();
						layout = (TiCompositeLayout.LayoutParams) view.getLayoutParams();
					} else {
						layout = (TiCompositeLayout.LayoutParams) view.getLayoutParams();
					}

					float eventX = Math.round(event.getRawX());
					float eventY = Math.round(event.getRawY());

					currentLeft = currentLeft == 0 ? view.getLeft() : currentLeft;
					currentTop = currentTop == 0 ? view.getTop() : currentTop;

					center.put("x", currentLeft + view.getWidth() / 2);
					center.put("y", currentTop + view.getHeight() / 2);

					props.put("left", 0);
					props.put("top", 0);
					props.put("center", center);

					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (hasListenerStart == false) {
							hasListenerStart = proxy.hasListeners("start");
						}

						if (hasListenerMove == false) {
							hasListenerMove = proxy.hasListeners("move");
						}

						if (hasListenerEnd == false) {
							hasListenerEnd = proxy.hasListeners("end");
						}

						startLeft = (currentLeft - eventX);
						startTop = (currentTop - eventY);
						startX = eventX;
						startY = eventY;
						isDragging = false;

						if (hasListenerStart) {
							props.put("left", view.getLeft());
							props.put("top", view.getTop());

							proxy.fireEvent("start", props);
						}
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						boolean isDraggingUp = eventY < (startY - threshold) && (eventX > (startX - threshold)) && (eventX < (startX + threshold));
						boolean isDraggingDown = eventY > (startY + threshold) && (eventX > (startX - threshold)) && (eventX < (startX + threshold));
						boolean isDraggingLeft = eventX < (startX - threshold) && (eventY > (startY - threshold))  && (eventY < (startY + threshold));
						boolean isDraggingRight = eventX > (startX + threshold) && (eventY > (startY - threshold)) && (eventY < (startY + threshold));

						if (proxy.getHasAxisX()) {
							positionTop = currentTop;
							positionLeft = (eventX + startLeft);

							if (proxy.hasMaxLeft && proxy.getMaxLeft() < positionLeft) {
								positionLeft = proxy.getMaxLeft();
							}

							if (proxy.hasMinLeft && proxy.getMinLeft() > positionLeft) {
								positionLeft = proxy.getMinLeft();
							}

							if (isDragging == false) {
								if (isDraggingUp || isDraggingDown) {
									view.getParent().requestDisallowInterceptTouchEvent(true);
								} else if (isDraggingLeft || isDraggingRight) {
									isDragging = true;
								}
							}
						} else if (proxy.getHasAxisY()) {
							positionTop = (eventY + startTop);
							positionLeft = currentLeft;

							if (isDragging == false) {
								if (isDraggingUp || isDraggingDown) {
									isDragging = true;
								} else if (isDraggingLeft || isDraggingRight) {
									view.getParent().requestDisallowInterceptTouchEvent(true);
								}
							}

							if (proxy.hasMaxTop == true && proxy.getMaxTop() < positionTop) {
								positionTop = proxy.getMaxTop();
							}

							if (proxy.hasMinTop == true && proxy.getMinTop() > positionTop) {
								positionTop = proxy.getMinTop();
							}
						}

						if (isDragging || proxy.getIsFree()) {
							capture = ! proxy.getIsFree();

							layout.optionLeft = new TiDimension(positionLeft, TiDimension.TYPE_LEFT);
							layout.optionTop = new TiDimension(positionTop, TiDimension.TYPE_TOP);

							if (proxy.getEnsureRight()) {
								layout.optionRight = new TiDimension(-positionLeft, TiDimension.TYPE_RIGHT);
							}

							if (proxy.getEnsureBottom()) {
								layout.optionBottom = new TiDimension(-positionTop, TiDimension.TYPE_BOTTOM);
							}

							view.setLayoutParams(layout);

							ViewGroup parentView = (ViewGroup) view.getParent();
							parentView.invalidate();

							if (lastTop > positionTop) {
								directionVertical = "up";
							} else if (lastTop < positionTop){
								directionVertical = "down";
							} else {
								directionVertical = "neutral";
							}

							if (lastLeft > positionLeft) {
								directionHorizontal = "left";
							} else if (lastLeft < positionLeft){
								directionHorizontal = "right";
							} else {
								directionHorizontal = "neutral";
							}

							lastTop = positionTop;
							lastLeft = positionLeft;

							if (hasListenerMove) {
								props.put("left", positionLeft);
								props.put("top", positionTop);

								proxy.fireEvent("move", props);
							}
						}
					} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
						if (isDragging) {
							positionLeft = proxy.getHasAxisY() ? currentLeft : (eventX + startLeft);
							positionTop = proxy.getHasAxisX() ? currentTop : (eventY + startTop);

							if (proxy.hasMaxLeft && proxy.getMaxLeft() < positionLeft) {
								positionLeft = proxy.getMaxLeft();
							}

							if (proxy.hasMinLeft && proxy.getMinLeft() > positionLeft) {
								positionLeft = proxy.getMinLeft();
							}

							if (proxy.hasMaxTop && proxy.getMaxTop() < positionTop) {
								positionTop = proxy.getMaxTop();
							}

							if (proxy.hasMinTop && proxy.getMinTop() > positionTop) {
								positionTop = proxy.getMinTop();
							}

							layout.optionLeft = new TiDimension(positionLeft, TiDimension.TYPE_LEFT);
							layout.optionTop = new TiDimension(positionTop, TiDimension.TYPE_TOP);

							if (proxy.getEnsureRight()) {
								layout.optionRight = new TiDimension(-positionLeft, TiDimension.TYPE_RIGHT);
							}

							if (proxy.getEnsureBottom()) {
								layout.optionBottom = new TiDimension(-positionTop, TiDimension.TYPE_BOTTOM);
							}

							view.setLayoutParams(layout);
						}

						currentTop = 0;
						currentLeft = 0;

						if (hasListenerEnd) {
							props.put("left", positionLeft);
							props.put("top", positionTop);

							KrollDict endProps = props;

							endProps.put("directionHorizontal", directionHorizontal);
							endProps.put("directionVertical", directionVertical);

							proxy.fireEvent(event.getAction() == MotionEvent.ACTION_CANCEL ? "cancel" : "end", endProps);
						}
					}
				}

				return capture;
			}
		});

		setNativeView(view);
	}

	@Override
	public void registerForTouch() {
		//
	}
}
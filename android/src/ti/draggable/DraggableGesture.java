/**
 * An enhanced fork of the original TiDraggable module by Pedro Enrique,
 * allows for simple creation of "draggable" views.
 *
 * Copyright (C) 2013 Seth Benjamin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * -- Original License --
 *
 * Copyright 2012 Pedro Enrique
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ti.draggable;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class DraggableGesture implements OnTouchListener
{
	protected TiUIView contentView;
	protected TiViewProxy proxy;
	protected WeakReference<ConfigProxy> config;
	protected VelocityTracker velocityTracker;
	protected ViewConfiguration vc;
	protected int threshold;
	protected double lastX = 0;
	protected double lastY = 0;
	protected double distanceX = 0;
	protected double distanceY = 0;
	protected double startLeft = 0;
	protected double startTop = 0;
	protected double lastLeft;
	protected double lastTop;
	public boolean isBeingDragged = false;

	public DraggableGesture(TiViewProxy proxy, TiUIView view, WeakReference<ConfigProxy> config)
	{
		this.proxy = proxy;
		this.contentView = view;
		this.vc = ViewConfiguration.get(this.contentView.getOuterView().getContext());
		this.threshold = vc.getScaledPagingTouchSlop();
		this.config = config;
	}

	public void determineDrag(MotionEvent event)
	{
		double xDelta = Math.abs(event.getX() - this.lastX);
		double yDelta = Math.abs(event.getY() - this.lastY);

		this.isBeingDragged = xDelta > this.threshold || yDelta > this.threshold;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		ConfigProxy weakConfig = this.config.get();

		if (TiConvert.toBoolean(weakConfig.getProperty("enabled")) == false)
		{
			return false;
		}

		if (! this.isBeingDragged && event.getAction() == MotionEvent.ACTION_MOVE)
		{
			this.determineDrag(event);
		}

		if (! this.isBeingDragged) {
			return false;
		}

		float screenX = event.getRawX(),
			  screenY = event.getRawY();

		View nativeView = this.contentView.getOuterView();
		KrollDict configProps = weakConfig.getProperties();

		if (this.velocityTracker != null)
		{
			this.velocityTracker.addMovement(event);
		}

		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.startDrag(event);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				this.stopDrag(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (this.isBeingDragged)
				{
					boolean xAxis = ! configProps.isNull("axis") && configProps.getString("axis").equals("x"),
							yAxis = ! configProps.isNull("axis") && configProps.getString("axis").equals("y"),
							noAxis = ! xAxis && ! yAxis;

					double leftEdge = screenX + startLeft,
						   topEdge = screenY + startTop;

					if (xAxis)
					{
						topEdge = nativeView.getY();
					}
					else if (yAxis)
					{
						leftEdge = nativeView.getX();
					}

					if (noAxis || xAxis)
					{
						if (! configProps.isNull("minLeft"))
						{
							double _minLeft = weakConfig.getDimensionAsPixels("minLeft");

							if (leftEdge <= _minLeft)
							{
								leftEdge = _minLeft;
							}
						}

						if (! configProps.isNull("maxLeft"))
						{
							double _maxLeft = weakConfig.getDimensionAsPixels("maxLeft");

							if (leftEdge >= _maxLeft)
							{
								leftEdge = _maxLeft;
							}
						}
					}

					if (noAxis || yAxis)
					{
						if (! configProps.isNull("minTop"))
						{
							double _minTop = weakConfig.getDimensionAsPixels("minTop");

							if (topEdge <= _minTop)
							{
								topEdge = _minTop;
							}
						}

						if (! configProps.isNull("maxTop"))
						{
							double _maxTop = weakConfig.getDimensionAsPixels("maxTop");

							if (topEdge >= _maxTop)
							{
								topEdge = _maxTop;
							}
						}
					}

					moveView(nativeView, leftEdge, topEdge);

					distanceX += Math.abs(lastLeft - leftEdge);
					distanceY += Math.abs(lastTop - topEdge);
					lastLeft = leftEdge;
					lastTop = topEdge;

					if (proxy.hasListeners("move"))
					{
						this.velocityTracker.computeCurrentVelocity(1000);

						KrollDict eventDict = new KrollDict();
						KrollDict velocityDict = new KrollDict();

						velocityDict.put("x", this.velocityTracker.getXVelocity());
						velocityDict.put("y", this.velocityTracker.getYVelocity());
						eventDict.put("left", nativeView.getX());
						eventDict.put("top", nativeView.getY());
						eventDict.put("velocity", velocityDict);

						proxy.fireEvent("move", eventDict);
					}
				}
				break;
		}

		return true;
	}

	public void startDrag(MotionEvent event)
	{
		this.lastX = event.getX();
		this.lastY = event.getY();

		View nativeView = this.contentView.getOuterView();

		float screenX = event.getRawX(),
			  screenY = event.getRawY();

		this.startLeft = nativeView.getX() - screenX;
		this.startTop = nativeView.getY() - screenY;
		this.lastLeft = nativeView.getX();
		this.lastTop = nativeView.getY();
		this.distanceX = this.distanceY = 0;

		if (this.velocityTracker == null)
		{
			this.velocityTracker = VelocityTracker.obtain();
		}

		if (proxy.hasListeners("start"))
		{
			this.velocityTracker.computeCurrentVelocity(1000);

			KrollDict eventDict = new KrollDict();
			KrollDict velocityDict = new KrollDict();

			velocityDict.put("x", this.velocityTracker.getXVelocity());
			velocityDict.put("y", this.velocityTracker.getYVelocity());
			eventDict.put("left", nativeView.getX());
			eventDict.put("top", nativeView.getY());
			eventDict.put("velocity", velocityDict);

			proxy.fireEvent("start", eventDict);
		}
	}

	public void stopDrag(MotionEvent event)
	{
		if (this.isBeingDragged)
		{
			this.isBeingDragged = false;

			if (proxy.hasListeners("end") || proxy.hasListeners("cancel"))
			{
				View nativeView = this.contentView.getOuterView();

				this.velocityTracker.computeCurrentVelocity(1000);

				KrollDict eventDict = new KrollDict();
				KrollDict velocityDict = new KrollDict();
				KrollDict distanceDict = new KrollDict();

				distanceDict.put("x", distanceX);
				distanceDict.put("y", distanceY);
				velocityDict.put("x", this.velocityTracker.getXVelocity());
				velocityDict.put("y", this.velocityTracker.getYVelocity());
				eventDict.put("left", nativeView.getX());
				eventDict.put("top", nativeView.getY());
				eventDict.put("velocity", velocityDict);
				eventDict.put("distance", distanceDict);

				proxy.fireEvent(event.getAction() == MotionEvent.ACTION_UP ? "end" : "cancel", eventDict);
			}

			this.velocityTracker.clear();
		}
	}

	protected void moveView(View view, double left, double top)
	{
		double translationLeft = lastLeft - left,
			   translationTop = lastTop - top;

		translateMappedProxies(translationLeft, translationTop);

		TiCompositeLayout.LayoutParams mappedLayout = (TiCompositeLayout.LayoutParams) view.getLayoutParams();
		
		mappedLayout.optionLeft = new TiDimension(left, TiDimension.TYPE_LEFT);
		mappedLayout.optionTop = new TiDimension(top, TiDimension.TYPE_TOP);

		view.setLayoutParams(mappedLayout);

		proxy.setProperty("left", new Double(left));
		proxy.setProperty("top", new Double(top));
	}

	protected void translateMappedProxies(double translationLeft, double translationTop)
	{
		View nativeView = this.contentView.getOuterView();
		ConfigProxy weakConfig = this.config.get();
		KrollDict configProps = weakConfig.getProperties();
		Object[] maps = (Object[]) configProps.get("maps");

		if (maps != null)
		{
			for (int i = 0; i < maps.length; i++)
			{
				@SuppressWarnings("unchecked")
				KrollDict map = new KrollDict((Map<? extends String, ? extends Object>) maps[i]);
				TiViewProxy mappedProxy = (TiViewProxy) map.get("view");
				View mappedView = mappedProxy.peekView().getOuterView();

				double parallaxAmount = map.optInt("parallaxAmount", 1),
					   newTop = mappedView.getY(),
					   newLeft = mappedView.getX();

				boolean shouldInvalidate = false;

				if (map.containsKeyAndNotNull("constrain"))
				{
					KrollDict constraints = map.getKrollDict("constrain");
					KrollDict xConstraint = constraints.getKrollDict("x");
					KrollDict yConstraint = constraints.getKrollDict("y");
					String constraintAxis = constraints.getString("axis");

					if (xConstraint != null || (constraintAxis != null && constraintAxis.equals("x")))
					{
						shouldInvalidate = true;

						Integer parentWidth = nativeView.getWidth(),
								xStart = xConstraint.getInt("start"),
								xEnd = xConstraint.getInt("end"),
								parentMinLeft = weakConfig.getDimensionAsPixels("minLeft"),
								parentMaxLeft = weakConfig.getDimensionAsPixels("maxLeft");

						double proxyWidth = mappedView.getWidth(),
							   xStartParallax = xStart / parallaxAmount,
							   xRatio = proxyWidth / (parentWidth.doubleValue() / 2),
							   xDistance = parentMaxLeft.doubleValue() - parentMinLeft.doubleValue(),
							   xWidth = proxyWidth / parallaxAmount;

						if (xStart != null || xEnd != null)
						{
							xWidth = Math.abs(xStartParallax) + Math.abs(xEnd.doubleValue());
						}

						if (parentMinLeft != null || parentMaxLeft != null)
						{
							xRatio = xDistance == 0 ? 1 : xWidth / xDistance;
						}

						newLeft -= translationLeft * xRatio;

						if (newLeft < xStartParallax)
						{
							newLeft = xStartParallax;
						}
						else if (newLeft > xEnd.doubleValue())
						{
							newLeft = xEnd.doubleValue();
						}
					}
					else if (constraintAxis.equals("x"))
					{
						shouldInvalidate = true;
						newLeft -= translationLeft / parallaxAmount;
					}

					if (yConstraint != null || (constraintAxis != null && constraintAxis.equals("y")))
					{
						shouldInvalidate = true;

						Integer parentHeight = nativeView.getHeight(),
								yStart = yConstraint.getInt("start"),
								yEnd = yConstraint.getInt("end"),
								parentMinTop = weakConfig.getDimensionAsPixels("minTop"),
								parentMaxTop = weakConfig.getDimensionAsPixels("maxTop");

						double proxyHeight = mappedView.getHeight(),
							   yStartParallax = yStart / parallaxAmount,
							   yRatio = proxyHeight / (parentHeight.doubleValue() / 2),
							   yDistance = parentMaxTop.doubleValue() - parentMinTop.doubleValue(),
							   yHeight = proxyHeight / parallaxAmount;

						if (yStart != null || yEnd != null)
						{
							yHeight = Math.abs(yStartParallax) + Math.abs(yEnd.doubleValue());
						}

						if (parentMinTop != null || parentMaxTop != null)
						{
							yRatio = yDistance == 0 ? 1 : yHeight / yDistance;
						}

						newTop -= translationTop * yRatio;

						if (newTop < yStartParallax)
						{
							newTop = yStartParallax;
						}
						else if (newTop > yEnd.doubleValue())
						{
							newTop = yEnd.doubleValue();
						}
					}
					else if (constraintAxis.equals("y"))
					{
						shouldInvalidate = true;
						newTop -= translationTop / parallaxAmount;
					}
				}
				else
				{
					shouldInvalidate = true;
					newLeft -= translationLeft / parallaxAmount;
					newTop -= translationTop / parallaxAmount;
				}

				if (shouldInvalidate)
				{
					mappedView.setX((float) newLeft);
					mappedView.setY((float) newTop);

					mappedProxy.setProperty("left", new Double(newLeft));
					mappedProxy.setProperty("top", new Double(newTop));

					mappedView.invalidate();
				}
			}
		}
	}

	protected void prepareMappedProxies(ConfigProxy configProxy)
	{
		KrollDict configProps = configProxy.getProperties();
		Object[] maps = (Object[]) configProps.get("maps");

		if (maps != null)
		{
			for (int i = 0; i < maps.length; i++)
			{
				@SuppressWarnings("unchecked")
				KrollDict map = new KrollDict((Map<? extends String, ? extends Object>) maps[i]);
				TiViewProxy mappedProxy = (TiViewProxy) map.get("view");
				View mappedView = mappedProxy.peekView().getOuterView();

				if (map.containsKeyAndNotNull("constrain"))
				{
					KrollDict constraints = map.getKrollDict("constrain");
					KrollDict constraintX = constraints.getKrollDict("x");
					KrollDict constraintY = constraints.getKrollDict("y");

					double proxyWidth = mappedView.getWidth(),
						   proxyHeight = mappedView.getHeight(),
						   parallaxAmount = map.optInt("parallaxAmount", 1),
						   newLeft = 0,
						   newTop = 0;

					boolean didModifyPosition = false;

					if (constraintX != null)
					{
						if (constraintX.containsKeyAndNotNull("start"))
						{
							didModifyPosition = true;
							newLeft = TiConvert.toDouble(constraintX.get("start")) / parallaxAmount;
							newLeft += proxyWidth;
						}
					}

					if (constraintY != null)
					{
						if (constraintY.containsKeyAndNotNull("start"))
						{
							didModifyPosition = true;
							newTop = TiConvert.toDouble(constraintX.get("start")) / parallaxAmount;
							newTop += proxyHeight;
						}
					}

					if (didModifyPosition)
					{
						TiCompositeLayout.LayoutParams mappedLayout = (TiCompositeLayout.LayoutParams) mappedView.getLayoutParams();
						mappedLayout.optionLeft = new TiDimension(newLeft, TiDimension.TYPE_LEFT);
						mappedLayout.optionTop = new TiDimension(newTop, TiDimension.TYPE_TOP);

						mappedView.setLayoutParams(mappedLayout);
					}
				}
			}
		}
	}
}
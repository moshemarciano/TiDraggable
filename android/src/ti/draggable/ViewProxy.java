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

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

@Kroll.proxy(creatableInModule = DraggableModule.class)
public class ViewProxy extends TiViewProxy {
	public ViewProxy self;
	public float maxTop = 0;
	public float minTop = 0;
	public float maxLeft = 0;
	public float minLeft = 0;
	public boolean ensureBottom = true;
	public boolean ensureRight = true;
	public boolean hasMaxTop = false;
	public boolean hasMinTop = false;
	public boolean hasMaxLeft = false;
	public boolean hasMinLeft = false;
	public boolean hasAxisX = false;
	public boolean hasAxisY = false;
	public boolean isDraggable = true;

	/*
	 * ViewProxy initializer
	 */
	public ViewProxy(TiContext tiContext) {
		self = this;
	}

	/*
	 * @see org.appcelerator.titanium.proxy.TiViewProxy#createView(android.app.Activity)
	 */
	@Override
	public TiUIView createView(Activity activity) {
		self.setAxis(self.getProperty("axis"));
		self.setMaxTop(self.getProperty("maxTop"));
		self.setMinTop(self.getProperty("minTop"));
		self.setMaxLeft(self.getProperty("maxLeft"));
		self.setMinLeft(self.getProperty("minLeft"));
		self.setIsDraggable(self.getProperty("isDraggable"));

		return new PEDraggableView(self, this);
	}

	/**
	 * Getter for determining if a view
	 * has no set axis
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getIsFree() {
		return (this.getHasAxisX() == false && this.getHasAxisY() == false);
	}

	/**
	 * Getter for isDraggable
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getIsDraggable() {
		return isDraggable;
	}

	/**
	 * Setter for isDraggable
	 *
	 * @param boolean isDraggable
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setIsDraggable(Object isDraggable) {
		if (isDraggable != null) {
			this.isDraggable = TiConvert.toBoolean(isDraggable);
		}
	}

	/**
	 * Setter for hasAxis(X|Y)
	 *
	 * @param string axis
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setAxis(Object axis) {
		if (axis != null) {
			axis = TiConvert.toString(axis);

			if (axis.equals("x")) {
				this.hasAxisX = true;
			} else if (axis.equals("y")) {
				this.hasAxisY = true;
			}
		}
	}

	/**
	 * Getter for hasAxisY
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getHasAxisY() {
		return this.hasAxisY;
	}

	/**
	 * Getter for hasAxisX
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getHasAxisX() {
		return this.hasAxisX;
	}

	/**
	 * Setter for maxTop
	 *
	 * @param float maxTop
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setMaxTop(Object maxTop) {
		if (maxTop != null) {
			maxTop = TiConvert.toString(maxTop);
			this.hasMaxTop = true;
			this.maxTop = new Float(maxTop.toString());
		} else {
			this.hasMaxTop = false;
			this.maxTop = 0;
		}
	}

	/**
	 * Getter for maxTop
	 *
	 * @return float
	 */
	@Kroll.method @Kroll.getProperty
	public float getMaxTop() {
		return this.maxTop;
	}

	/**
	 * Setter for minTop
	 *
	 * @param float minTop
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setMinTop(Object minTop) {
		if (minTop != null) {
			minTop = TiConvert.toString(minTop);
			this.hasMinTop = true;
			this.minTop = new Float(minTop.toString());
		} else {
			this.hasMinTop = false;
			this.minTop = 0;
		}
	}

	/**
	 * Getter for minTop
	 *
	 * @return float
	 */
	@Kroll.method @Kroll.getProperty
	public float getMinTop() {
		return this.minTop;
	}

	/**
	 * Setter for maxLeft
	 *
	 * @param float maxLeft
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setMaxLeft(Object maxLeft) {
		if (maxLeft != null) {
			maxLeft = TiConvert.toString(maxLeft);
			this.hasMaxLeft = true;
			this.maxLeft = new Float(maxLeft.toString());
		} else {
			this.hasMaxLeft = false;
			this.maxLeft = 0;
		}
	}

	/**
	 * Getter for maxLeft
	 *
	 * @return float
	 */
	@Kroll.method @Kroll.getProperty
	public float getMaxLeft() {
		return this.maxLeft;
	}

	/**
	 * Setter for minLeft
	 *
	 * @param float minLeft
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setMinLeft(Object minLeft) {
		if (minLeft != null) {
			minLeft = TiConvert.toString(minLeft);
			this.hasMinLeft = true;
			this.minLeft = new Float(minLeft.toString());
		} else {
			this.hasMinLeft = false;
			this.minLeft = 0;
		}
	}

	/**
	 * Getter for minLeft
	 *
	 * @return float
	 */
	@Kroll.method @Kroll.getProperty
	public float getMinLeft() {
		return this.minLeft;
	}

	/**
	 * Setter for ensureBottom
	 *
	 * @param boolean ensureBottom
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setEnsureBottom(boolean ensureBottom) {
		this.ensureBottom = ensureBottom;
	}

	/**
	 * Getter for ensureBottom
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getEnsureBottom() {
		return this.ensureBottom;
	}

	/**
	 * Setter for ensureRight
	 *
	 * @param boolean ensureRight
	 * @return void
	 */
	@Kroll.method @Kroll.setProperty
	public void setEnsureRight(boolean ensureRight) {
		this.ensureRight = ensureRight;
	}

	/**
	 * Getter for ensureRight
	 *
	 * @return boolean
	 */
	@Kroll.method @Kroll.getProperty
	public boolean getEnsureRight() {
		return this.ensureRight;
	}
}
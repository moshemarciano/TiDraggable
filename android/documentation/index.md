# TiDraggable - Native Draggable Views

An enhanced fork of the original [TiDraggable](https://github.com/pec1985/TiDraggable) module by [Pedro](http://twitter.com/pecdev) [Enrique](https://github.com/pec1985), allows for simple creation of "draggable" views.

## Installation

In your `tiapp.xml` file add the following to the `modules` node:

    <module version="1.2.4" platform="android">ti.draggable<module>

## Usage

```
var Draggable = require('ti.draggable'),
    mainWindow = Ti.UI.createWindow({
        backgroundColor : 'white'
    }),
    draggableView = Draggable.createView({
        width : 100,
        height : 100,
        backgroundColor : 'black'
    });

mainWindow.add(draggableView);
mainWindow.open();
```

    If you are building the Android module, make sure you update the .classpath and build.properties files to match your setup.

## Module Reference

### Draggable.createView(viewOptions);

Create a draggable view, as this view extends a normal Titanium view all properties and methods are inherited.

- viewOptions (Object):
    - Object containing view properties

## Draggable View Reference

### DraggableView.setIsDraggable(isDraggable);

Set whether or not a view can be draggable.

- isDraggable (Boolean):
    - Draggable status flag

### DraggableView.setAxis(axis);

Set which axis a view can be dragged on.

- axis (String):
    - Axis in which the draggable view will be constrained. Can either be `x` or `y`.

### DraggableView.setMinLeft(minLeftBoundary);

Set the minimum left-most boundary on a view.

- minLeftBoundary (Integer|null):
    - The minimum value the left side of the draggable view can be dragged.

### DraggableView.setMaxLeft(maxLeftBoundary);

Set the maximum left-most boundary on a view.

- maxLeftBoundary (Integer|null):
    - The maximum value the left side of the draggable view can be dragged.

### DraggableView.setMinTop(minTopBoundary);

Set the minimum top-most boundary on a view.

- minTopBoundary (Integer|null):
    - The minimum value the top side of the draggable view can be dragged.

### DraggableView.setMaxTop(maxTopBoundary);

Set the maximum top-most boundary on a view.

- maxTopBoundary (Integer|null):
    - The maximum value the top side of the draggable view can be dragged.

### DraggableView.setEnsureRight(ensureRight);

If a view is created without a set dimension width, it's size will be updated as it moves. This is not desirable in most situations.

- ensureRight (Boolean):
    - Boolean flag that when set to true will ensure the `right` properties are the opposite the `left`.

### DraggableView.setEnsureBottom(ensureBottom);

If a view is created without a set dimension height, it's size will be updated as it moves. This is not desirable in most situations.

- ensureBottom (Boolean):
    - Boolean flag that when set to true will ensure the `bottom` properties are the opposite the `top`.

### DraggableView.setCanRotate(canRotate);

Sets the view to be rotateable. (two-finger rotate gesture)

- canRotate (Boolean):
    - Boolean flag to enable or disable rotation.

> Note: This method has not been implement for Android.

### DraggableView.setCanResize(canResize);

Sets the view to be scaleable (two-finger pinch gesture).

- canResize (Boolean):
    - Boolean flag to enable or disable resizing.

> Note: This method has not been implement for Android.

## Credits & Notes

The work is largely based on [Pedro](http://twitter.com/pecdev) [Enrique's](https://github.com/pec1985) [TiDraggable](https://github.com/pec1985/TiDraggable) module license under the MIT (V2) license.
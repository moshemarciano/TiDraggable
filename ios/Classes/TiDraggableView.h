//
//  TiDraggableView.h
//  draggable
//
//  Created by Pedro Enrique on 1/21/12.
//  Copyright 2012 Pedro Enrique
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import "TiUIView.h"

@interface TiDraggableView : TiUIView<UIGestureRecognizerDelegate>
{
	NSString *axis;

	CGFloat left;
	CGFloat top;
	CGFloat maxLeft;
	CGFloat minLeft;
	CGFloat maxTop;
	CGFloat minTop;

	BOOL isDraggable;
    BOOL hasMaxLeft;
    BOOL hasMaxTop;
    BOOL hasMinLeft;
    BOOL hasMinTop;
    BOOL hasMoved;
}

@end
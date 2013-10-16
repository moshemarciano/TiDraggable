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

#import "TiDraggableGesture.h"

@implementation TiViewProxy (DraggableProxy)

- (void)setDraggable:(id)args
{
    if ([args isKindOfClass:[NSDictionary class]])
    {
        [self replaceValue:args forKey:@"draggable" notification:NO];
    }
    else if ([args isKindOfClass:[NSArray class]])
    {
        if ([args count] >= 2)
        {
            NSString* key;
            id value = [args objectAtIndex:1];
            
            ENSURE_ARG_AT_INDEX(key, args, 0, NSString);
            
            NSMutableDictionary* params = [[self valueForKey:@"draggable"] mutableCopy];
            
            if (! params)
            {
                params = [[NSMutableDictionary alloc] init];
            }
            
            [params setValue:value forKeyPath:key];
            
            [self replaceValue:[[params copy] autorelease] forKey:@"draggable" notification:YES];
            
            [params release];
        }
        else
        {
            ENSURE_SINGLE_ARG(args, NSDictionary);
            
            [self replaceValue:args forKey:@"draggable" notification:YES];
        }
    }
}

@end

@implementation TiDraggableGesture

- (id)initWithView:(TiUIView*)view andOptions:(NSDictionary *)options
{
    if (self = [super init])
    {
        self.view = view;
        self.properties = options;
        
        axis = [self.properties objectForKey:@"axis"];
        maxLeft = [[self.properties objectForKey:@"maxLeft"] floatValue];
        minLeft = [[self.properties objectForKey:@"minLeft"] floatValue];
        maxTop = [[self.properties objectForKey:@"maxTop"] floatValue];
        minTop = [[self.properties objectForKey:@"minTop"] floatValue];
        hasMaxLeft = [self.properties objectForKey:@"maxLeft"] != nil;
        hasMinLeft = [self.properties objectForKey:@"minLeft"] != nil;
        hasMaxTop = [self.properties objectForKey:@"maxTop"] != nil;
        hasMinTop = [self.properties objectForKey:@"minTop"] != nil;
        ensureRight = [TiUtils boolValue:[self.properties objectForKey:@"ensureRight"] def:NO];
        ensureBottom = [TiUtils boolValue:[self.properties objectForKey:@"ensureBottom"] def:NO];
        
        TiViewProxy* proxy = (TiViewProxy*)[self.view proxy];
        
        self.modelDelegate = [proxy modelDelegate];
        
        [proxy setModelDelegate:self];
        [proxy setProxyObserver:self];
        
        [self.view addGestureRecognizer:[[[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panDetected:)] autorelease]];
    }
    
    return self;
}

- (void)dealloc
{
    RELEASE_TO_NIL_AUTORELEASE(self.view);
    RELEASE_TO_NIL_AUTORELEASE(self.properties);
    RELEASE_TO_NIL_AUTORELEASE(self.mappedPoxies);
    RELEASE_TO_NIL(axis);
    
    [super dealloc];
}

- (void)propertyChanged:(NSString *)key oldValue:(id)oldValue newValue:(id)newValue proxy:(TiProxy *)proxy
{
    if ([key isEqualToString:@"draggable"])
    {
        self.properties = newValue;
        
        axis = [self.properties objectForKey:@"axis"];
        maxLeft = [[self.properties objectForKey:@"maxLeft"] floatValue];
        minLeft = [[self.properties objectForKey:@"minLeft"] floatValue];
        maxTop = [[self.properties objectForKey:@"maxTop"] floatValue];
        minTop = [[self.properties objectForKey:@"minTop"] floatValue];
        hasMaxLeft = [self.properties objectForKey:@"maxLeft"] != nil;
        hasMinLeft = [self.properties objectForKey:@"minLeft"] != nil;
        hasMaxTop = [self.properties objectForKey:@"maxTop"] != nil;
        hasMinTop = [self.properties objectForKey:@"minTop"] != nil;
        ensureRight = [TiUtils boolValue:[self.properties objectForKey:@"ensureRight"] def:NO];
        ensureBottom = [TiUtils boolValue:[self.properties objectForKey:@"ensureBottom"] def:NO];
        
        [self prepareMappedProxies];
    }
    
    if (self.modelDelegate)
    {
        [self.modelDelegate propertyChanged:key oldValue:oldValue newValue:newValue proxy:proxy];
    }
}

- (void)proxyDidRelayout:(id)sender
{
    if (! proxyDidLayout)
    {
        [self prepareMappedProxies];
    }
    
    proxyDidLayout = YES;
}

- (void)panDetected:(UIPanGestureRecognizer *)panRecognizer
{
    ENSURE_UI_THREAD_1_ARG(panRecognizer);
    
    CGPoint translation = [panRecognizer translationInView:self.view];
    CGPoint imageViewPosition = self.view.center;
    CGSize size = self.view.frame.size;
    
    [self mapProxyOriginToCollection:[self.properties objectForKey:@"maps"]
                    withTranslationX:translation.x
                     andTranslationY:translation.y];
    
    if([axis isEqualToString:@"x"])
    {
        imageViewPosition.x += translation.x;
        imageViewPosition.y = imageViewPosition.y;
    }
    else if([axis isEqualToString:@"y"])
    {
        imageViewPosition.x = imageViewPosition.x;
        imageViewPosition.y += translation.y;
    }
    else
    {
        imageViewPosition.x += translation.x;
        imageViewPosition.y += translation.y;
    }
    
    if(hasMaxLeft || hasMaxTop || hasMinLeft || hasMinTop)
    {
        if(hasMaxLeft && imageViewPosition.x - size.width / 2 > maxLeft)
        {
            imageViewPosition.x = maxLeft + size.width / 2;
        }
        else if(hasMinLeft && imageViewPosition.x - size.width / 2 < minLeft)
        {
            imageViewPosition.x = minLeft + size.width / 2;
        }
        
        if(hasMaxTop && imageViewPosition.y - size.height / 2 > maxTop)
        {
            imageViewPosition.y = maxTop + size.height / 2;
        }
        else if(hasMinTop && imageViewPosition.y - size.height / 2 < minTop)
        {
            imageViewPosition.y = minTop + size.height / 2;
        }
    }
    
    self.view.center = imageViewPosition;
    
    [panRecognizer setTranslation:CGPointZero inView:self.view];
    
    float left = self.view.frame.origin.x;
    float top = self.view.frame.origin.y;
    
    [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:top] forKey:@"top" notification:YES];
    [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:left] forKey:@"left" notification:YES];
    
    if (ensureRight)
    {
        [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:-left] forKey:@"right" notification:YES];
    }
    
    if (ensureBottom)
    {
        [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:-top] forKey:@"bottom" notification:YES];
    }
    
    NSDictionary *tiProps = [NSDictionary dictionaryWithObjectsAndKeys:
                             [NSNumber numberWithFloat:left], @"left",
                             [NSNumber numberWithFloat:top], @"top",
                             [TiUtils pointToDictionary:self.view.center], @"center",
                             nil];
    
    if([self.view.proxy _hasListeners:@"start"] && [panRecognizer state] == UIGestureRecognizerStateBegan)
    {
        [self.view.proxy fireEvent:@"start" withObject:tiProps];
    }
    else if([self.view.proxy _hasListeners:@"move"] && [panRecognizer state] == UIGestureRecognizerStateChanged)
    {
        [self.view.proxy fireEvent:@"move" withObject:tiProps];
    }
    else if([self.view.proxy _hasListeners:@"end"] && [panRecognizer state] == UIGestureRecognizerStateEnded)
    {
        [self.view.proxy fireEvent:@"end" withObject:tiProps];
    }
    else if([self.view.proxy _hasListeners:@"cancel"] && [panRecognizer state] == UIGestureRecognizerStateCancelled)
    {
        [self.view.proxy fireEvent:@"cancel" withObject:tiProps];
    }
}

- (void)prepareMappedProxies
{
    NSArray* maps = [self.properties objectForKey:@"maps"];
    
    if ([maps isKindOfClass:[NSArray class]])
    {
        [maps enumerateObjectsUsingBlock:^(id map, NSUInteger index, BOOL *stop) {
            TiViewProxy* proxy = [map objectForKey:@"view"];
            NSDictionary* constraints = [map objectForKey:@"constrain"];
            NSDictionary* constraintX = [constraints objectForKey:@"x"];
            NSDictionary* constraintY = [constraints objectForKey:@"y"];
            BOOL fromCenterX = [TiUtils boolValue:[constraintX objectForKey:@"fromCenter"] def:NO];
            BOOL fromCenterY = [TiUtils boolValue:[constraintY objectForKey:@"fromCenter"] def:NO];
            
            NSNumber* multiplier = [TiUtils numberFromObject:[map objectForKey:@"multiplier"]];
            
            if (! multiplier)
            {
                multiplier = [NSNumber numberWithInteger:1];
            }
            
            CGSize proxySize = [proxy view].frame.size;
            CGPoint proxyCenter = [proxy view].center;
            
            if (constraintX)
            {
                NSNumber* startX = [constraintX objectForKey:@"start"];
                
                if (startX)
                {
                    if (fromCenterX)
                    {
                        proxyCenter.x = [startX floatValue] + [proxy.parent view].frame.size.width / 2;
                    }
                    else
                    {
                        proxyCenter.x = ([startX floatValue] / [multiplier floatValue]) + (proxySize.width / 2);
                    }
                }
            }
            
            if (constraintY)
            {
                NSNumber* startY = [constraintY objectForKey:@"start"];
                
                if (startY)
                {
                    if (fromCenterY)
                    {
                        proxyCenter.y = [startY floatValue] + [proxy.parent view].frame.size.height / 2;
                    }
                    else
                    {
                        proxyCenter.y = ([startY floatValue] / [multiplier floatValue]) + (proxySize.height / 2);
                    }
                }
            }
            
            if (constraintY || constraintX)
            {
                [proxy view].center = proxyCenter;
            }
            
            if (constraintY)
            {
                [proxy setTop:[NSNumber numberWithFloat:[proxy view].frame.origin.y]];
            }
            
            if (constraintX)
            {
                [proxy setLeft:[NSNumber numberWithFloat:[proxy view].frame.origin.x]];
            }
        }];
    }
}

- (void)mapProxyOriginToCollection:(NSArray*)proxies withTranslationX:(float)translationX andTranslationY:(float)translationY
{
    if ([proxies isKindOfClass:[NSArray class]])
    {
        [proxies enumerateObjectsUsingBlock:^(id map, NSUInteger index, BOOL *stop) {
            TiViewProxy* proxy = [map objectForKey:@"view"];
            
            NSNumber* multiplier = [TiUtils numberFromObject:[map objectForKey:@"multiplier"]];
            
            if (! multiplier)
            {
                multiplier = [NSNumber numberWithInteger:1];
            }
            
            CGPoint proxyCenter = [proxy view].center;
            CGSize proxySize = [proxy view].frame.size;
            CGSize parentSize = [[proxy.parent view] frame].size;
            
            NSDictionary* constraints = [map objectForKey:@"constrain"];
            NSDictionary* constraintX = [constraints objectForKey:@"x"];
            NSDictionary* constraintY = [constraints objectForKey:@"y"];
            
            if (constraintX)
            {
                BOOL fromCenterX = [TiUtils boolValue:[constraintX objectForKey:@"fromCenter"] def:NO];
                NSNumber* startX = [constraintX objectForKey:@"start"];
                NSNumber* endX = [constraintX objectForKey:@"end"];
                float offsetWidth = proxySize.width;
                
                if (startX && endX)
                {
                    startX = [NSNumber numberWithFloat:[startX floatValue] / [multiplier floatValue]];
                    offsetWidth = [endX floatValue] - [startX floatValue];
                }
                else
                {
                    offsetWidth /= [multiplier floatValue];
                }
                
                float ratioW = (offsetWidth / 2) / (proxySize.width / 2);
                
                proxyCenter.x += translationX * ratioW;
                
                if(startX && endX)
                {
                    if (fromCenterX)
                    {
                        startX = [NSNumber numberWithFloat:[startX floatValue] + (parentSize.width / 2 - proxySize.width / 2)];
                        endX = [NSNumber numberWithFloat:[endX floatValue] + (parentSize.width / 2 - proxySize.width / 2)];
                    }
                    
                    if(endX && proxyCenter.x - proxySize.width / 2 > [endX floatValue])
                    {
                        proxyCenter.x = [endX floatValue] + proxySize.width / 2;
                    }
                    else if(startX && proxyCenter.x - proxySize.width / 2 < [startX floatValue])
                    {
                        proxyCenter.x = [startX floatValue] + proxySize.width / 2;
                    }
                }
            }
            
            if (constraintY)
            {
                BOOL fromCenterY = [TiUtils boolValue:[constraintY objectForKey:@"fromCenter"] def:NO];
                NSNumber* startY = [constraintY objectForKey:@"start"];
                NSNumber* endY = [constraintY objectForKey:@"end"];
                float offsetHeight = proxySize.height;
                
                if (startY && endY)
                {
                    startY = [NSNumber numberWithFloat:[startY floatValue] / [multiplier floatValue]];
                    offsetHeight = [endY floatValue] - [startY floatValue];
                }
                else
                {
                    offsetHeight /= [multiplier floatValue];
                }
                
                float ratioH = (offsetHeight / 2) / (proxySize.height / 2);
                
                proxyCenter.y += translationY * ratioH;
                
                if(startY && endY)
                {
                    if (fromCenterY)
                    {
                        startY = [NSNumber numberWithFloat:[startY floatValue] + (parentSize.height / 2 - proxySize.height / 2)];
                        endY = [NSNumber numberWithFloat:[endY floatValue] + (parentSize.height / 2 - proxySize.height / 2)];
                    }
                    
                    if(endY && proxyCenter.y - proxySize.height / 2 > [endY floatValue])
                    {
                        proxyCenter.y = [endY floatValue] + proxySize.height / 2;
                    }
                    else if(startY && proxyCenter.y - proxySize.height / 2 < [startY floatValue])
                    {
                        proxyCenter.y = [startY floatValue] + proxySize.height / 2;
                    }
                }
            }
            else
            {
                proxyCenter.x += translationX;
                proxyCenter.y += translationY;
            }
            
            [proxy view].center = proxyCenter;
            
            [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:[proxy view].frame.origin.x]
                                                   forKey:@"left"
                                             notification:YES];
            
            [(TiViewProxy*)[self.view proxy] replaceValue:[NSNumber numberWithFloat:[proxy view].frame.origin.y]
                                                   forKey:@"top"
                                             notification:YES];
        }];
    }
}

- (void)tandemAnimation
{
    NSTimer* animationInterval = [NSTimer scheduledTimerWithTimeInterval:0.01
                                                                  target:self
                                                                selector:@selector(animationTrigger)
                                                                userInfo:nil
                                                                 repeats:YES];
    
    [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationCurveEaseInOut animations:^{
        [_view setCenter:CGPointZero];
    } completion:^(BOOL finished) {
        [animationInterval invalidate];
        lastAnimationFrame = CGRectZero;
    }];
}

- (void)animationTrigger
{
    CGRect currentFrame = [[self.view.layer presentationLayer] frame];
    
    float translationX = 0;
    float translationY = 0;
    
    if (! CGRectEqualToRect(lastAnimationFrame, CGRectZero))
    {
        translationX = currentFrame.origin.x - lastAnimationFrame.origin.x;
        translationY = currentFrame.origin.y - lastAnimationFrame.origin.y;
    }
    
    [self mapProxyOriginToCollection:[self.properties objectForKey:@"maps"]
                    withTranslationX:translationX
                     andTranslationY:translationY];
    
    lastAnimationFrame = currentFrame;
}

@end
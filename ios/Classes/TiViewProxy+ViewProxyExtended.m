#import "TiViewProxy+ViewProxyExtended.h"

@implementation TiViewProxy (ViewProxyExtended)

- (void)respositionEx
{
    if (! repositioning)
	{
		ENSURE_UI_THREAD_0_ARGS
        
		repositioning = YES;
        
        UIView *parentView = [parent parentViewForChild:self];
        CGSize referenceSize = (parentView != nil) ? parentView.bounds.size : sandboxBounds.size;
        
		positionCache = PositionConstraintGivenSizeBoundsAddingResizing(&layoutProperties, self, sizeCache.size,
                                                                        [[view layer] anchorPoint], referenceSize, sandboxBounds.size, &autoresizeCache);
        
		positionCache.x += sizeCache.origin.x + sandboxBounds.origin.x;
		positionCache.y += sizeCache.origin.y + sandboxBounds.origin.y;
        
		[view setCenter:positionCache];
		[self refreshPosition];
        
		repositioning = NO;
	}

}

@end
package ti.draggable;

import java.lang.ref.WeakReference;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.util.TiConvert;

import android.view.View;

@Kroll.proxy
public class ConfigProxy extends KrollProxy implements KrollProxyListener
{
	protected KrollDict dimensions = new KrollDict();
	protected WeakReference<DraggableImpl> draggableImpl;
	
	public ConfigProxy(KrollDict config)
	{
		super();
		
		Object[] maps = null;

		if (config != null)
		{			
			if (config.containsKeyAndNotNull("maps"))
			{
				maps = (Object[]) config.get("maps");
			}
		}
		
		properties.put("ensureRight", TiConvert.toBoolean(config, "ensureRight", false));
		properties.put("ensureBottom", TiConvert.toBoolean(config, "ensureBottom", false));
		properties.put("minLeft", config.containsKeyAndNotNull("minLeft") ? TiConvert.toDouble(config, "minLeft") : null);
		properties.put("maxLeft", config.containsKeyAndNotNull("minLeft") ? TiConvert.toDouble(config, "maxLeft") : null);
		properties.put("minTop", config.containsKeyAndNotNull("minTop") ? TiConvert.toDouble(config, "minTop") : null);
		properties.put("maxTop", config.containsKeyAndNotNull("maxTop") ? TiConvert.toDouble(config, "maxTop") : null);
		properties.put("axis", config.containsKeyAndNotNull("axis") ? TiConvert.toString(config, "axis") : null);
		properties.put("maps", maps);

		dimensions.put("minLeft", config.containsKeyAndNotNull("minLeft") ? TiConvert.toTiDimension(config, "minLeft", TiDimension.TYPE_LEFT) : null);
		dimensions.put("maxLeft", config.containsKeyAndNotNull("minLeft") ? TiConvert.toTiDimension(config, "maxLeft", TiDimension.TYPE_LEFT) : null);
		dimensions.put("minTop", config.containsKeyAndNotNull("minTop") ? TiConvert.toTiDimension(config, "minTop", TiDimension.TYPE_TOP) : null);
		dimensions.put("maxTop", config.containsKeyAndNotNull("maxTop") ? TiConvert.toTiDimension(config, "maxTop", TiDimension.TYPE_TOP) : null);
		
		setModelListener(this);
	}
	
	public Integer getDimensionAsPixels(String key)
	{
		TiDimension dimension = (TiDimension) dimensions.get(key);
		
		if (dimension != null)
		{
			View decorView = TiApplication.getAppCurrentActivity().getWindow().getDecorView();
			
			return dimension.getAsPixels(decorView);
		}
		
		return null;
	}
	
	public void setDraggableImpl(WeakReference<DraggableImpl> draggableView)
	{
		draggableImpl = draggableView; 
	}
	
	public DraggableImpl getDraggableImpl()
	{
		return draggableImpl != null ? draggableImpl.get() : null;
	}
	
	@Kroll.method
	public void setConfig(Object[] args)
	{
		if (args.length >= 2)
		{
			setPropertyAndFire((String) args[0], args[1]);
		}
		else if (args.length == 1)
		{
			applyProperties(args[0]);
		}
	}

	@Override
	public void listenerAdded(String type, int count, KrollProxy proxy)
	{
		// Unused
	}

	@Override
	public void listenerRemoved(String type, int count, KrollProxy proxy)
	{
		// Unused
	}

	@Override
	public void processProperties(KrollDict properties)
	{
		// Unused
	}

	@Override
	public void propertiesChanged(List<KrollPropertyChange> changes, KrollProxy proxy)
	{
		// Unused
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) 
	{
		if (key.equals("maps"))
		{
			DraggableImpl impl = getDraggableImpl();
			
			if (impl != null)
			{
				getDraggableImpl().listener.prepareMappedProxies(this);
			}
		}
		else if (key.equals("minLeft") || key.equals("maxLeft"))
		{
			dimensions.put(key, TiConvert.toTiDimension(TiConvert.toString(newValue), TiDimension.TYPE_LEFT));
		}
		else if (key.equals("minTop") || key.equals("maxTop"))
		{
			dimensions.put(key, TiConvert.toTiDimension(TiConvert.toString(newValue), TiDimension.TYPE_TOP));
		}
	}
}
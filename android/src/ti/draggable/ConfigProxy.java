package ti.draggable;

import java.lang.ref.WeakReference;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;

@Kroll.proxy
public class ConfigProxy extends KrollProxy implements KrollProxyListener
{
	protected WeakReference<DraggableImpl> draggableImpl;
	
	public ConfigProxy(KrollDict config)
	{
		super();
		
		boolean ensureRight = false;
		boolean ensureBottom = false;
		Integer minLeft = null;
		Integer maxLeft = null;
		Integer minTop = null;
		Integer maxTop = null;
		String axis = null;
		Object[] maps = null;

		if (config != null)
		{
			if (config.containsKeyAndNotNull("ensureRight"))
			{
				ensureRight = config.getBoolean("ensureRight");
			}
			
			if (config.containsKeyAndNotNull("ensureBottom"))
			{
				ensureBottom = config.getBoolean("ensureBottom");
			}
			
			if (config.containsKeyAndNotNull("minLeft"))
			{
				minLeft = config.getInt("minLeft");
			}

			if (config.containsKeyAndNotNull("maxLeft"))
			{
				maxLeft = config.getInt("maxLeft");
			}

			if (config.containsKeyAndNotNull("minTop"))
			{
				minTop = config.getInt("minTop");
			}

			if (config.containsKeyAndNotNull("maxTop"))
			{
				maxTop = config.getInt("maxTop");
			}

			if (config.containsKeyAndNotNull("axis"))
			{
				axis = config.getString("axis");
			}
			
			if (config.containsKeyAndNotNull("maps"))
			{
				maps = (Object[]) config.get("maps");
			}
		}
		
		properties.put("ensureRight", ensureRight);
		properties.put("ensureBottom", ensureBottom);
		properties.put("minLeft", minLeft);
		properties.put("maxLeft", maxLeft);
		properties.put("minTop", minTop);
		properties.put("maxTop", maxTop);
		properties.put("axis", axis);
		properties.put("maps", maps);
		
		setModelListener(this);
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
			if (newValue != null)
			{
				DraggableModule.debugLog(newValue.toString());
			}
			
			DraggableImpl impl = getDraggableImpl();
			
			if (impl != null)
			{
				getDraggableImpl().listener.prepareMappedProxies(properties);
			}
		}
	}
}
package ride.android.com.dps.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;


import java.util.ArrayList;
import java.util.List;

import ride.android.com.dps.R;

public class RouteOverlay {
	protected List<Marker> stationMarkers = new ArrayList<Marker>();
	protected List<Polyline> allPolyLines = new ArrayList<Polyline>();
	protected Marker startMarker;
	protected Marker endMarker;
	protected LatLng startPoint;
	protected LatLng endPoint;
	protected AMap mAMap;
	private Context mContext;
	private Bitmap startBit, endBit, busBit, walkBit, driveBit;
	protected boolean nodeIconVisible = true;

	public RouteOverlay(Context context) {
		mContext = context;
	}


	public void removeFromMap() {
		if (startMarker != null) {
			startMarker.remove();

		}
		if (endMarker != null) {
			endMarker.remove();
		}
		for (Marker marker : stationMarkers) {
			marker.remove();
		}
		for (Polyline line : allPolyLines) {
			line.remove();
		}
		destroyBit();
	}

	private void destroyBit() {
		if (startBit != null) {
			startBit.recycle();
			startBit = null;
		}
		if (endBit != null) {
			endBit.recycle();
			endBit = null;
		}
		if (busBit != null) {
			busBit.recycle();
			busBit = null;
		}
		if (walkBit != null) {
			walkBit.recycle();
			walkBit = null;
		}
		if (driveBit != null) {
			driveBit.recycle();
			driveBit = null;
		}
	}

	protected BitmapDescriptor getStartBitmapDescriptor() {
		return BitmapDescriptorFactory.fromResource(R.drawable.amap_start);
	}

	protected BitmapDescriptor getEndBitmapDescriptor() {
		return BitmapDescriptorFactory.fromResource(R.drawable.amap_end);
	}


	protected BitmapDescriptor getRideBitmapDescriptor(){
		return BitmapDescriptorFactory.fromResource(R.drawable.amap_ride);
	}

	protected void addStartAndEndMarker() {
		startMarker = mAMap.addMarker((new MarkerOptions())
				.position(startPoint).icon(getStartBitmapDescriptor())
				.title("\u8D77\u70B9"));

		endMarker = mAMap.addMarker((new MarkerOptions()).position(endPoint)
				.icon(getEndBitmapDescriptor()).title("\u7EC8\u70B9"));

	}

	public void zoomToSpan() {
		if (startPoint != null) {
			if (mAMap == null)
				return;
			try {
				LatLngBounds bounds = getLatLngBounds();
				mAMap.animateCamera(CameraUpdateFactory
						.newLatLngBounds(bounds, 50));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	protected LatLngBounds getLatLngBounds() {
		LatLngBounds.Builder b = LatLngBounds.builder();
		b.include(new LatLng(startPoint.latitude, startPoint.longitude));
		b.include(new LatLng(endPoint.latitude, endPoint.longitude));
		return b.build();
	}

	public void setNodeIconVisibility(boolean visible) {
		try {
			nodeIconVisible = visible;
			if (this.stationMarkers != null && this.stationMarkers.size() > 0) {
				for (int i = 0; i < this.stationMarkers.size(); i++) {
					this.stationMarkers.get(i).setVisible(visible);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	protected void addStationMarker(MarkerOptions options) {
		if(options == null) {
			return;
		}
		Marker marker = mAMap.addMarker(options);
		if(marker != null) {
			stationMarkers.add(marker);
		}
		
	}

	protected void addPolyLine(PolylineOptions options) {
		if(options == null) {
			return;
		}
		Polyline polyline = mAMap.addPolyline(options);
		if(polyline != null) {
			allPolyLines.add(polyline);
		}
	}
	
	protected float getRouteWidth() {
		return 18f;
	}

	protected int getWalkColor() {
		return Color.parseColor("#6db74d");
	}


	protected int getBusColor() {
		return Color.parseColor("#537edc");
	}

	protected int getDriveColor() {
		return Color.parseColor("#537edc");
	}

	protected int getRideColor(){
		return Color.parseColor("#537edc");
	}

}

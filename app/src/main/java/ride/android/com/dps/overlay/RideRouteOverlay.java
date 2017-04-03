package ride.android.com.dps.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideStep;


import java.util.List;

import ride.android.com.dps.R;
import ride.android.com.dps.util.AMapUtil;


public class RideRouteOverlay extends RouteOverlay {

	private PolylineOptions mPolylineOptions;
	
	private BitmapDescriptor rideStationDescriptor= null;

	private RidePath ridePath;

	public RideRouteOverlay(Context context, AMap amap, RidePath path,
							LatLonPoint start, LatLonPoint end) {
		super(context);
		this.mAMap = amap;
		this.ridePath = path;
		startPoint = AMapUtil.convertToLatLng(start);
		endPoint = AMapUtil.convertToLatLng(end);
	}

	public void addToMap() {
		
		initPolylineOptions();
		try {
			List<RideStep> ridePaths = ridePath.getSteps();
			mPolylineOptions.add(startPoint);
			for (int i = 0; i < ridePaths.size(); i++) {
				RideStep rideStep = ridePaths.get(i);
				LatLng latLng = AMapUtil.convertToLatLng(rideStep
						.getPolyline().get(0));
				
				addRideStationMarkers(rideStep, latLng);
				addRidePolyLines(rideStep);
			}
			mPolylineOptions.add(endPoint);
			addStartAndEndMarker();
			
			showPolyline();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	private void addRidePolyLines(RideStep rideStep) {
		mPolylineOptions.addAll(AMapUtil.convertArrList(rideStep.getPolyline()));
	}

	private void addRideStationMarkers(RideStep rideStep, LatLng position) {
		addStationMarker(new MarkerOptions()
				.position(position)
				.title("\u65B9\u5411:" + rideStep.getAction()
						+ "\n\u9053\u8DEF:" + rideStep.getRoad())
				.snippet(rideStep.getInstruction()).visible(nodeIconVisible)
				.anchor(0.5f, 0.5f).icon(rideStationDescriptor));
	}
	

    private void initPolylineOptions() {
		rideStationDescriptor = getRideBitmapDescriptor();
    	if(rideStationDescriptor == null) {
			rideStationDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.amap_ride);
    	}
        mPolylineOptions = null;
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(getRideColor()).width(getRouteWidth());
    }
	 private void showPolyline() {
	        addPolyLine(mPolylineOptions);
	    }
}

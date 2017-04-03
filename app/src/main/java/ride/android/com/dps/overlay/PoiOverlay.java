package ride.android.com.dps.overlay;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;


public class PoiOverlay {
	private List<PoiItem> mPois;
	private AMap mAMap;
	private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();

	public PoiOverlay(AMap amap, List<PoiItem> pois) {
		mAMap = amap;
		mPois = pois;
	}

	public void addToMap() {
		try{
			for (int i = 0; i < mPois.size(); i++) {
				Marker marker = mAMap.addMarker(getMarkerOptions(i));
				marker.setObject(i);
				mPoiMarks.add(marker);
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public void removeFromMap() {
		for (Marker mark : mPoiMarks) {
			mark.remove();
		}
	}

	public void zoomToSpan() {
		try{
			if (mPois != null && mPois.size() > 0) {
				if (mAMap == null)
					return;
				if(mPois.size()==1){
					mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mPois.get(0).getLatLonPoint().getLatitude(),
							mPois.get(0).getLatLonPoint().getLongitude()), 18f));
				}else{
					LatLngBounds bounds = getLatLngBounds();
					mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
				}
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	private LatLngBounds getLatLngBounds() {
		LatLngBounds.Builder b = LatLngBounds.builder();
		for (int i = 0; i < mPois.size(); i++) {
			b.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
					mPois.get(i).getLatLonPoint().getLongitude()));
		}
		return b.build();
	}

	private MarkerOptions getMarkerOptions(int index) {
		return new MarkerOptions()
				.position(
						new LatLng(mPois.get(index).getLatLonPoint()
								.getLatitude(), mPois.get(index)
								.getLatLonPoint().getLongitude()))
				.title(getTitle(index)).snippet(getSnippet(index))
				.icon(getBitmapDescriptor(index));
	}

	protected BitmapDescriptor getBitmapDescriptor(int index) {
		return null;
	}

	protected String getTitle(int index) {
		return mPois.get(index).getTitle();
	}

	protected String getSnippet(int index) {
		return mPois.get(index).getSnippet();
	}

	public int getPoiIndex(Marker marker) {
		for (int i = 0; i < mPoiMarks.size(); i++) {
			if (mPoiMarks.get(i).equals(marker)) {
				return i;
			}
		}
		return -1;
	}

	public PoiItem getPoiItem(int index) {
		if (index < 0 || index >= mPois.size()) {
			return null;
		}
		return mPois.get(index);
	}
}

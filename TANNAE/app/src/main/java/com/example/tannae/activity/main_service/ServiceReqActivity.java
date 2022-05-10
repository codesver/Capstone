package com.example.tannae.activity.main_service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tannae.R;
import com.example.tannae.network.Network;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

// << ServiceReq Activity >>
public class ServiceReqActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {
    private RadioGroup rgLocation;
    private TextView tvOrigin, tvDestination;
    private Button btnServiceReq, btnBack;
    private Switch switchShare;
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private MapPoint mapPoint;
    private MapPOIItem marker;
    private boolean locationType = true;
    private String originLocation = "", destinationLocation = "";
    private double originLat= 0, originLong = 0, destinationLat = 0, destinationLong = 0;

    // < onCreate >
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicereq);
        // Setting
        setViews();
        setEventListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view_servicereq);

        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);

        marker = new MapPOIItem();
        marker.setItemName("위치");
        marker.setTag(0);
        marker.setMapPoint(mapView.getMapCenterPoint());
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mapViewContainer.addView(mapView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewContainer.removeView(mapView);
    }

    // < Register views >
    private void setViews() {
        rgLocation = findViewById(R.id.rg_location_servicereq);
        btnServiceReq = findViewById(R.id.btn_request_servicereq);
        btnBack = findViewById(R.id.btn_back_servicereq);
        tvOrigin = findViewById(R.id.tv_origin_servicereq);
        tvDestination = findViewById(R.id.tv_destination_servicereq);
        switchShare = findViewById(R.id.switch_share_servicereq);
    }

    // < Register event listeners >
    private void setEventListeners() {
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        rgLocation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                locationType = (checkedId == R.id.rb_origin_servicereq) ? true : false;
            }
        });


        // Request Service [SOCKET]
        btnServiceReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Create JSON
                    JSONObject start = new JSONObject();
                    start.put("name", originLocation);
                    start.put("lat", originLat);
                    start.put("long", originLong);
                    JSONObject end = new JSONObject();
                    end.put("name", destinationLocation);
                    end.put("lat", destinationLat);
                    end.put("long", destinationLong);
                    JSONObject user = new JSONObject();
                    //////////////////////////////////////////////////// 현재 로그인되어 있는 User(SQLite 에 저장된) 정보를 json 형태로 전환
                    JSONObject data = new JSONObject();
                    data.put("start", start);
                    data.put("end", end);
                    data.put("share", switchShare.isChecked());
                    data.put("user", user);
                    Network.socket.emit("requestService", data);
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    intent.putExtra("type", false);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // < BackPress >
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // < Map Methods >
    @Override
    public void onMapViewInitialized(MapView mapView) {
        mapPoint = mapView.getMapCenterPoint();
        MapReverseGeoCoder mapGeoCoder = new MapReverseGeoCoder("be32c53145962ae88db090324e2223b0",
                mapPoint, this, ServiceReqActivity.this);
        mapGeoCoder.startFindingAddress();

        if (locationType) {
            originLat = mapPoint.getMapPointGeoCoord().latitude;
            originLong = mapPoint.getMapPointGeoCoord().longitude;
        } else {
            destinationLat = mapPoint.getMapPointGeoCoord().latitude;
            destinationLong = mapPoint.getMapPointGeoCoord().longitude;
        }
        marker.setMapPoint(mapPoint);
        mapView.addPOIItem(marker);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        mapPoint = mapView.getMapCenterPoint();
        MapReverseGeoCoder mapGeoCoder = new MapReverseGeoCoder("be32c53145962ae88db090324e2223b0",
                mapPoint, this, ServiceReqActivity.this);
        mapGeoCoder.startFindingAddress();

        if (locationType) {
            originLat = mapPoint.getMapPointGeoCoord().latitude;
            originLong = mapPoint.getMapPointGeoCoord().longitude;
        } else {
            destinationLat = mapPoint.getMapPointGeoCoord().latitude;
            destinationLong = mapPoint.getMapPointGeoCoord().longitude;
        }
        marker.setMapPoint(mapPoint);
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        if (locationType) {
            tvOrigin.setText(s);
            originLocation = s;
        } else {
            tvDestination.setText(s);
            destinationLocation = s;
        }
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        if (locationType)
            tvOrigin.setText("올바른 지역이 아닙니다.");
        else
            tvDestination.setText("올바른 지역이 아닙니다.");
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}

package com.jj.game.boost.logtools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.dao.DelayLostSaveDao;
import com.jj.game.boost.domain.DelayLostSave;
import com.jj.game.boost.utils.LogUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * create by huzedong
 */
public class ChartFragmentMoblie extends AbstractFragment {
    private LineChartView chart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 12;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];
    private DelayLostSave[] NumbersTab;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor;
    private boolean hasGradientToTransparent = false;

    public ChartFragmentMoblie() {
    }

    public void refreshChart(){
        generateData();
        prepareDataAnimation();
        chart.startDataAnimation();
        chart.setZoomEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setViewportCalculationEnabled(false);

        resetViewport();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

        chart = (LineChartView) rootView.findViewById(R.id.chart);
        chart.setOnValueTouchListener(new ValueTouchListener());

        // Generate some random values.
        generateValues();

        generateData();
        prepareDataAnimation();
        chart.startDataAnimation();
        chart.setZoomEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        // Disable viewport recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        resetViewport();
        return rootView;
    }

    private void generateValues() {

    }

    private void reset() {
        numberOfLines = 1;

        hasAxes = true;
        hasAxesNames = true;
        hasLines = true;
        hasPoints = true;
        shape = ValueShape.CIRCLE;
        isFilled = false;
        hasLabels = false;
        isCubic = false;
        hasLabelForSelected = false;
        pointsHaveDifferentColor = false;

        chart.setValueSelectionEnabled(hasLabelForSelected);
        resetViewport();
    }
    private void resetViewport() {
        DelayLostSaveDao dao = JJBoostApplication.getDaoInstant();

        QueryBuilder<DelayLostSave> qb = dao.queryBuilder()
//                .orderAsc(DelayLostSaveDao.Properties.Id)
                .orderDesc(DelayLostSaveDao.Properties.Id)
                .limit(1800);
        List<DelayLostSave> list = qb.list();
//        Collections.reverse(list);
        double min = Double.MAX_VALUE;
        double max = 0;
        for (int i = 0; i < list.size(); i++){
            int delay = Integer.valueOf(list.get(i).getDelay());
            if(max < delay){
                max = delay;
            }
            if(min > delay){
                min = delay;
            }
        }
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = list.size() == 0 ? 100:(float)max;
        v.left = 0;
        v.right = list.size();
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    private void generateData() {
        DelayLostSaveDao dao = JJBoostApplication.getDaoInstant();
        QueryBuilder<DelayLostSave> qb = dao.queryBuilder()
//                .orderAsc(DelayLostSaveDao.Properties.Id)
                .orderDesc(DelayLostSaveDao.Properties.Id)
                .limit(1800);
        List<DelayLostSave> list = qb.list();
        LogUtil.e("huzedong", " list.size() : " + list.size());
//        Collections.reverse(list);
        List<Line> lines = new ArrayList<Line>();
        List<PointValue> values = new ArrayList<PointValue>();
        Line line = new Line(values);
        for (int i = 0; i < list.size(); i++) {
            values.add(new PointValue(i, Integer.valueOf(list.get(i).getDelay())));
        }
        line.setColor(Color.parseColor("#ffc107"));
        if (pointsHaveDifferentColor){
            line.setPointColor(Color.parseColor("#ffc107"));
        }
        line.setShape(shape);
        line.setCubic(isCubic);
        line.setFilled(isFilled);
        line.setHasLabels(hasLabels);
        line.setPointRadius(3);
        line.setStrokeWidth(2);
        line.setHasLabelsOnlyForSelected(hasLabelForSelected);
        line.setHasLines(hasLines);
        line.setHasPoints(!hasPoints);
//            line.setHasGradientToTransparent(hasGradientToTransparent);

        lines.add(line);

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();// X轴属性
            Axis axisY = new Axis().setHasLines(true);

//            axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
            axisX.setTextColor(Color.GRAY);  //设置字体颜色
            //axisX.setName("date");  //表格名称
            axisX.setTextSize(8);//设置字体大小
            axisX.setMaxLabelChars(6); //最多几个X轴坐标
            //data.setAxisXTop(axisX);  //x 轴在顶部
//            axisX.setHasLines(true); //x 轴分割线
            ArrayList<AxisValue> axisValuesX = new ArrayList<AxisValue>();//定义X轴刻度值的数据集合
            for (int i=0; i < list.size(); i++){
                axisValuesX.add(new AxisValue(i).setLabel(String.valueOf(i)));
            }
            axisX.setValues(axisValuesX);//为X轴显示的刻度值设置数据集合

            if (hasAxesNames) {
                axisX.setName("最近的点(最多展示最新1800个)/左侧是最新点");
                axisY.setName("延迟 ms");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }
        chart.setZoomEnabled(false);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
    }

    /**
     * Adds lines to data, after that data should be set again with
     * {@link LineChartView#setLineChartData(LineChartData)}. Last 4th line has non-monotonically x values.
     */
    private void addLineToData() {
        if (data.getLines().size() >= maxNumberOfLines) {
            Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ++numberOfLines;
        }

        generateData();
    }

    private void toggleLines() {
        hasLines = !hasLines;

        generateData();
    }

    private void togglePoints() {
        hasPoints = !hasPoints;

        generateData();
    }

    private void toggleGradient() {
        hasGradientToTransparent = !hasGradientToTransparent;

        generateData();
    }

    private void toggleCubic() {
        isCubic = !isCubic;

        generateData();

        if (isCubic) {
            // It is good idea to manually set a little higher max viewport for cubic lines because sometimes line
            // go above or below max/min. To do that use Viewport.inest() method and pass negative value as dy
            // parameter or just set top and bottom values manually.
            // In this example I know that Y values are within (0,100) range so I set viewport height range manually
            // to (-5, 105).
            // To make this works during animations you should use Chart.setViewportCalculationEnabled(false) before
            // modifying viewport.
            // Remember to set viewport after you call setLineChartData().
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = -5;
            v.top = 105;
            // You have to set max and current viewports separately.
            chart.setMaximumViewport(v);
            // I changing current viewport with animation in this case.
            chart.setCurrentViewportWithAnimation(v);
        } else {
            // If not cubic restore viewport to (0,100) range.
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = 0;
            v.top = 100;

            // You have to set max and current viewports separately.
            // In this case, if I want animation I have to set current viewport first and use animation listener.
            // Max viewport will be set in onAnimationFinished method.
            chart.setViewportAnimationListener(new ChartAnimationListener() {

                @Override
                public void onAnimationStarted() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationFinished() {
                    // Set max viewpirt and remove listener.
                    chart.setMaximumViewport(v);
                    chart.setViewportAnimationListener(null);

                }
            });
            // Set current viewpirt with animation;
            chart.setCurrentViewportWithAnimation(v);
        }

    }

    private void toggleFilled() {
        isFilled = !isFilled;

        generateData();
    }

    private void togglePointColor() {
        pointsHaveDifferentColor = !pointsHaveDifferentColor;

        generateData();
    }

    private void setCircles() {
        shape = ValueShape.CIRCLE;

        generateData();
    }

    private void setSquares() {
        shape = ValueShape.SQUARE;

        generateData();
    }

    private void setDiamonds() {
        shape = ValueShape.DIAMOND;

        generateData();
    }

    private void toggleLabels() {
        hasLabels = !hasLabels;

        if (hasLabels) {
            hasLabelForSelected = false;
            chart.setValueSelectionEnabled(hasLabelForSelected);
        }

        generateData();
    }

    private void toggleLabelForSelected() {
        hasLabelForSelected = !hasLabelForSelected;

        chart.setValueSelectionEnabled(hasLabelForSelected);

        if (hasLabelForSelected) {
            hasLabels = false;
        }

        generateData();
    }

    private void toggleAxes() {
        hasAxes = !hasAxes;

        generateData();
    }

    private void toggleAxesNames() {
        hasAxesNames = !hasAxesNames;

        generateData();
    }

    private void prepareDataAnimation() {
//        DaoSession session = JJBoostApplication.getDaoInstant();
//        PointSaveDao dao = session.getPointSaveDao();
//        QueryBuilder<DelayLostSave> qb = dao.queryBuilder().where(PointSaveDao.Properties.Mode.eq(0))
//                .orderDesc(PointSaveDao.Properties.Id)
//                .limit(6);
//        List<DelayLostSave> list = qb.list();
//        Collections.reverse(list);

//        for (Line line : data.getLines()) {
//            for(int i = 0; i < line.getValues().size(); i++){
//                PointValue pv = line.getValues().get(i);
//                pv.setTarget(i, pv.getY());
//            }
//        }
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
//            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }
}

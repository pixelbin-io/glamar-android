//
// Created by imkal001 on 16/9/19.
//

#ifndef ARBEAUTY2_COMMON_UTILITIES_H
#define ARBEAUTY2_COMMON_UTILITIES_H

/*
 * This is a wrapper around cv::fillpoly
 */

void fillpoly_x(cv::Mat& image,const std::vector<cv::Point>& points,cv::Scalar color);

/*
 * For brightness control
*/
void gammaCorrection(cv::Mat &img, const double gamma_);

/*
* To interpolate a vector of cv::Point
* Used to approximate cubic curves for which we have few points.
*/
std::vector<cv::Point> tinyspline_cubic_interpolation( const std::vector<cv::Point>& points);

// sorting comparators
bool x_based_ascending_sorting(const cv::Point &i, const cv::Point &j);
bool y_based_ascending_sorting(const cv::Point &i, const cv::Point &j);

/*
* This function can be used to expand or contract a  curve by repositioning a point on the perpendicular
* drawn to the tangent at that point on the curve.
 * Return: Its return is an empty vector if either the passed point vector is empty or have a single element
 *          Otherwise it return a vector of point for the rescaled curve scaled by scaling_factor(input).
*/

std::vector<cv::Point> rescale_curve(const std::vector<cv::Point>& points,const float scaling_factor);
#endif //ARBEAUTY2_COMMON_UTILITIES_H

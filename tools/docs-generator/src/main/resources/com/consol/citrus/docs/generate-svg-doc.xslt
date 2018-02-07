<?xml version="1.0" encoding="UTF-8"?>
<!-- 
 * Copyright 2006-2010 the original author or authors.
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
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:spring="http://www.springframework.org/schema/beans" 
	xmlns:citrus="http://www.citrusframework.org/schema/testcase"	
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns="http://www.w3.org/2000/svg"
	exclude-result-prefixes="spring citrus">
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" doctype-public="http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"/>
	
	<xsl:template match="spring:beans">
		<xsl:apply-templates select="citrus:testcase" />
	</xsl:template>
	
	<xsl:template match="/spring:beans/citrus:testcase">
		<svg xmlns:svg="http://www.w3.org/2000/svg" version="1.0" width="210mm" height="297mm">
			<title><xsl:value-of select="@name"/></title>
   			<desc><xsl:value-of select="citrus:description/text()"/></desc>
   			<defs>
				<linearGradient id="lnrGradientStart"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#00ff00;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<linearGradient id="lnrGradientStop"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#ff0000;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<linearGradient id="lnrGradientSend"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#aaccff;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<linearGradient id="lnrGradientReceive"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#ff9955;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<linearGradient id="lnrGradientEcho"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#ffeeaa;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<linearGradient id="lnrGradientSleep"
					gradientTransform="scale(1.40,0.75)" x1="65" y1="70" x2="8" y2="15" gradientUnits="userSpaceOnUse">
					<stop id="stop1" style="stop-color:#c8b7b7;stop-opacity:1" offset="0" />
					<stop id="stop2" style="stop-color:#ffffff;stop-opacity:1" offset="1" />
				</linearGradient>
				<symbol id="start">
					<rect width="104" height="54" rx="20" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="20" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientStart);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="20">START</text>
				</symbol>
				<symbol id="send">
					<rect width="104" height="54" rx="0" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="0" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientSend);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="30">send</text>
					<use x="75" y="35" xlink:href="#sendIcon"/>
				</symbol>
				<symbol id="sendIcon">
					<path d="M 5.5881868,23.379738 C 5.5881868,23.379738 16.619842,6.3980609 28.866779,15.556415 C 30.130987,16.501799 31.569537,8.7265271 31.569537,8.7265271 L 44.385838,24.621536 L 27.994921,29.340366 C 27.994921,29.340366 29.685919,23.196674 28.518036,22.51048 C 19.647798,17.298733 15.265804,30.085443 15.265804,30.085443 L 5.5881868,23.379738 z"
						style="fill:#87aade;fill-opacity:1;fill-rule:evenodd;stroke:#000000;stroke-width:1.39602351px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" />
				</symbol>
				<symbol id="receive">
					<rect width="104" height="54" rx="0" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="0" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientReceive);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="20">receive</text>
					<use x="75" y="35" xlink:href="#receiveIcon"/>
				</symbol>
				<symbol id="receiveIcon">
					<path d="M 46.385838,23.379738 C 46.385838,23.379738 35.354183,6.3980609 23.107246,15.556415 C 21.843038,16.501799 20.404488,8.7265271 20.404488,8.7265271 L 7.5881868,24.621536 L 23.979104,29.340366 C 23.979104,29.340366 22.288106,23.196674 23.455989,22.51048 C 32.326227,17.298733 36.708221,30.085443 36.708221,30.085443 L 46.385838,23.379738 z"
						style="fill:#c83737;fill-opacity:1;fill-rule:evenodd;stroke:#000000;stroke-width:1.39602351px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" />
				</symbol>
				<symbol id="echo">
					<rect width="104" height="54" rx="0" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="0" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientEcho);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="30">echo</text>
					<use x="75" y="28" xlink:href="#echoIcon"/>
				</symbol>
				<symbol id="echoIcon">
					<path d="M 52.883118,22.38961 A 20.987013,11.688312 0 1 1 10.909092,22.38961 A 20.987013,11.688312 0 1 1 52.883118,22.38961 z"
						transform="translate(-6.8311688,-5.5844156)" 
						style="opacity:1;fill:#c8c4b7;fill-opacity:1;stroke:#000000;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 10.077921,24.623376 C 10.79638,32.393861 10.637431,36.504863 5.6103892,42.389611 C 15.103884,33.974057 21.938859,27.937934 29.61039,28.259741"
						style="fill:#c8c4b7;fill-opacity:1;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1" />
					<path d="M 14.753247,11.766234 L 38.12987,11.87013"
						style="fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:8, 1;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 9.7922081,15.454546 L 33.168831,15.558442" 
						style="fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:6, 1;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 15.792208,19.454546 L 39.168831,19.558442" 
						style="fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:8, 1;stroke-dashoffset:0;stroke-opacity:1" />
				</symbol>
				<symbol id="sleep">
					<rect width="104" height="54" rx="0" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="0" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientSleep);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="30">sleep</text>
					<use x="75" y="28" xlink:href="#sleepIcon"/>
				</symbol>
				<symbol id="sleepIcon">
					<path d="M 41.454546,21.870131 C 41.454546,30.907518 33.918975,38.233767 24.623377,38.233767 C 15.327779,38.233767 7.7922076,30.907518 7.7922077,21.870131 C 7.7922076,12.832744 15.327779,5.5064947 24.623377,5.5064947 C 33.918975,5.5064947 41.454546,12.832744 41.454546,21.870131 L 41.454546,21.870131 z"
						transform="matrix(1.1535182,0,0,1.1864759,-4.7476684,-2.7470865)"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 90.597403,31.636364 C 90.597403,40.731132 83.015316,48.103897 73.662339,48.103897 C 64.309361,48.103897 56.727274,40.731132 56.727274,31.636364 C 56.727274,22.541596 64.309361,15.168831 73.662339,15.168831 C 83.015316,15.168831 90.597403,22.541596 90.597403,31.636364 L 90.597403,31.636364 z"
						transform="matrix(0.9743099,0,0,1.0019716,-48.205008,-8.5104244)"
						style="opacity:1;fill:#ffffff;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 24.755335,31.153973 C 24.516284,30.543838 24.298177,29.926924 24.081052,29.30869 C 23.643208,28.040508 23.189103,26.77811 22.760029,25.506809 C 22.301103,24.061351 21.800046,22.630191 21.281984,21.205054 C 20.817725,19.982151 20.415076,18.73588 19.93695,17.518473 C 19.612574,16.681968 19.239685,15.864934 18.875247,15.045241 C 18.716908,14.687439 18.566248,14.317493 18.352578,13.988282 L 19.664761,13.225771 C 19.86727,13.571365 20.009898,13.952244 20.163251,14.321324 C 20.512412,15.15213 20.850377,15.988462 21.175301,16.829011 C 21.638767,18.055126 22.040862,19.304224 22.490443,20.535566 C 23.003216,21.963458 23.500539,23.39648 23.972571,24.838427 C 24.40023,26.106122 24.869449,27.358117 25.33631,28.611693 C 25.575246,29.218691 25.802687,29.831469 26.104045,30.41118 L 24.755335,31.153973 z"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 19.26613,29.053776 C 19.4413,28.505868 19.824258,28.041668 20.157169,27.582427 C 20.294248,27.40176 20.427713,27.218293 20.568406,27.040427 C 20.727739,26.838995 21.52427,25.879326 21.659216,25.717143 C 22.401944,24.824512 23.14551,23.933053 23.897582,23.048271 C 25.503222,21.163057 27.205421,19.364302 28.955071,17.61287 C 29.937919,16.614524 30.904571,15.60049 31.921766,14.636841 C 32.491454,14.081234 33.106919,13.575463 33.690649,13.035435 C 33.868005,12.870387 34.039042,12.698572 34.20048,12.517966 L 35.600741,11.922129 C 35.446875,12.123769 35.258172,12.296338 35.072333,12.468612 C 34.478701,13.028437 33.822608,13.521665 33.240714,14.095427 C 32.188919,15.072979 31.185174,16.099435 30.16702,17.111734 C 28.384703,18.849738 26.661818,20.648813 25.05956,22.555469 C 24.328794,23.426666 23.609789,24.30615 22.895171,25.190619 C 22.884368,25.203989 21.908101,26.415032 21.853899,26.485734 C 21.722056,26.657711 21.59929,26.836462 21.471985,27.011825 C 21.169536,27.446405 20.843218,27.88162 20.65483,28.380388 L 19.26613,29.053776 z"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="22.233767" y="7.2727275"  
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="22.233765" y="35.064934" 
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="-24.831169" y="35.584415" transform="matrix(0,-1,1,0,0,0)"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="-24.727272" y="7.5844154" transform="matrix(0,-1,1,0,0,0)"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
				</symbol>
				<symbol id="wait"> <!-- TODO CD what has to be done here? -->
					<rect width="104" height="54" rx="0" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="0" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientSleep);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="30">sleep</text>
					<use x="75" y="28" xlink:href="#sleepIcon"/>
				</symbol>
				<symbol id="waitIcon">
					<path d="M 41.454546,21.870131 C 41.454546,30.907518 33.918975,38.233767 24.623377,38.233767 C 15.327779,38.233767 7.7922076,30.907518 7.7922077,21.870131 C 7.7922076,12.832744 15.327779,5.5064947 24.623377,5.5064947 C 33.918975,5.5064947 41.454546,12.832744 41.454546,21.870131 L 41.454546,21.870131 z"
						transform="matrix(1.1535182,0,0,1.1864759,-4.7476684,-2.7470865)"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 90.597403,31.636364 C 90.597403,40.731132 83.015316,48.103897 73.662339,48.103897 C 64.309361,48.103897 56.727274,40.731132 56.727274,31.636364 C 56.727274,22.541596 64.309361,15.168831 73.662339,15.168831 C 83.015316,15.168831 90.597403,22.541596 90.597403,31.636364 L 90.597403,31.636364 z"
						transform="matrix(0.9743099,0,0,1.0019716,-48.205008,-8.5104244)"
						style="opacity:1;fill:#ffffff;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 24.755335,31.153973 C 24.516284,30.543838 24.298177,29.926924 24.081052,29.30869 C 23.643208,28.040508 23.189103,26.77811 22.760029,25.506809 C 22.301103,24.061351 21.800046,22.630191 21.281984,21.205054 C 20.817725,19.982151 20.415076,18.73588 19.93695,17.518473 C 19.612574,16.681968 19.239685,15.864934 18.875247,15.045241 C 18.716908,14.687439 18.566248,14.317493 18.352578,13.988282 L 19.664761,13.225771 C 19.86727,13.571365 20.009898,13.952244 20.163251,14.321324 C 20.512412,15.15213 20.850377,15.988462 21.175301,16.829011 C 21.638767,18.055126 22.040862,19.304224 22.490443,20.535566 C 23.003216,21.963458 23.500539,23.39648 23.972571,24.838427 C 24.40023,26.106122 24.869449,27.358117 25.33631,28.611693 C 25.575246,29.218691 25.802687,29.831469 26.104045,30.41118 L 24.755335,31.153973 z"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<path d="M 19.26613,29.053776 C 19.4413,28.505868 19.824258,28.041668 20.157169,27.582427 C 20.294248,27.40176 20.427713,27.218293 20.568406,27.040427 C 20.727739,26.838995 21.52427,25.879326 21.659216,25.717143 C 22.401944,24.824512 23.14551,23.933053 23.897582,23.048271 C 25.503222,21.163057 27.205421,19.364302 28.955071,17.61287 C 29.937919,16.614524 30.904571,15.60049 31.921766,14.636841 C 32.491454,14.081234 33.106919,13.575463 33.690649,13.035435 C 33.868005,12.870387 34.039042,12.698572 34.20048,12.517966 L 35.600741,11.922129 C 35.446875,12.123769 35.258172,12.296338 35.072333,12.468612 C 34.478701,13.028437 33.822608,13.521665 33.240714,14.095427 C 32.188919,15.072979 31.185174,16.099435 30.16702,17.111734 C 28.384703,18.849738 26.661818,20.648813 25.05956,22.555469 C 24.328794,23.426666 23.609789,24.30615 22.895171,25.190619 C 22.884368,25.203989 21.908101,26.415032 21.853899,26.485734 C 21.722056,26.657711 21.59929,26.836462 21.471985,27.011825 C 21.169536,27.446405 20.843218,27.88162 20.65483,28.380388 L 19.26613,29.053776 z"
						style="opacity:1;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="22.233767" y="7.2727275"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="22.233765" y="35.064934"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="-24.831169" y="35.584415" transform="matrix(0,-1,1,0,0,0)"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
					<rect width="2.2857144" height="3.6363637" x="-24.727272" y="7.5844154" transform="matrix(0,-1,1,0,0,0)"
						style="opacity:1;fill:#8c0e0a;fill-opacity:1;stroke:none;stroke-width:1;stroke-linecap:butt;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1" />
				</symbol>
				<symbol id="end">
					<rect width="104" height="54" rx="20" ry="25" x="5" y="5"
						style="fill:#000000;fill-opacity:0.15;stroke:none;stroke-width:1.8;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<rect width="100" height="50" rx="20" ry="25" x="5" y="5"
						style="fill:url(#lnrGradientStop);fill-opacity:1;stroke:#000000;stroke-width:2.5;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:10;stroke-opacity:1" />
					<text style="font-size:16pt;font-style:normal;font-weight:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Arial" xml:space="preserve" y="35" x="33">END</text>
				</symbol>
				<marker id="arrow" refX="0" refY="0" orient="auto" style="overflow:visible">
					<path d="M 8.7185878,4.0337352 L -2.2072895,0.016013256 L 8.7185884,-4.0017078 C 6.97309,-1.6296469 6.9831476,1.6157441 8.7185878,4.0337352 z" transform="scale(0.6,0.6)matrix(-1.1,0,0,-1.1,-1.1,0)" />
				</marker>
			</defs>
   			<g>
				<text x="20" y="50" style="font-size:36px">
					TestCase: <xsl:value-of select="@name"/>
				</text>
				
				<text x="20" y="70" style="font-size:14px;fill:grey">
					<xsl:call-template name="word-wrap">
						<xsl:with-param name="tobewrapped" select="normalize-space(citrus:description/text())"/>
						<xsl:with-param name="linelength" select="110"/>
					</xsl:call-template>
				</text>
				<g>
					<use x="200" y="150" xlink:href="#start"/>
					
					<xsl:call-template name="arrow">
						<xsl:with-param name="startPoint">255,210</xsl:with-param>
						<xsl:with-param name="endPoint">255,250</xsl:with-param>
					</xsl:call-template>
					
					<xsl:apply-templates select="citrus:actions/citrus:send | citrus:actions/citrus:receive | citrus:actions/citrus:echo | citrus:actions/citrus:sleep"/>
					
					<xsl:element name="use">
						<xsl:attribute name="x">200</xsl:attribute>
						<xsl:attribute name="y"><xsl:value-of select="250 + (count(citrus:actions/citrus:send)+count(citrus:actions/citrus:receive)+count(citrus:actions/citrus:echo)+count(citrus:actions/citrus:sleep))*100"/></xsl:attribute>
						<xsl:attribute name="xlink:href">#end</xsl:attribute>
					</xsl:element>
				</g>
			</g>
		</svg>
	</xsl:template>
	
	<xsl:template match="citrus:send | citrus:receive">
		<xsl:variable name="yPos" select="150 + (position() * 100)"/>
		<xsl:element name="use">
			<xsl:attribute name="x">200</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:attribute name="xlink:href">#<xsl:value-of select="name()"/></xsl:attribute>
		</xsl:element>
		
		<xsl:element name="text">
			<xsl:attribute name="x">350</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:attribute name="style">font-size:14px;fill:black;stroke:blue;stroke-opacity:0.5</xsl:attribute>
			<tspan x="350" dy="20"><xsl:value-of select="@type"/></tspan>
			<xsl:if test="citrus:description">
				<tspan x="350" dy="20" style="font-size:10px;fill:grey;">
					"<xsl:value-of select="substring(citrus:description, 1, 80)"/>
					<xsl:if test="string-length(citrus:description)>80"> ...</xsl:if>
					"
				</tspan>
			</xsl:if>
		</xsl:element>
		
		<xsl:call-template name="arrow">
			<xsl:with-param name="startPoint"><xsl:value-of select="concat('255,', (($yPos)+60))"/></xsl:with-param>
			<xsl:with-param name="endPoint"><xsl:value-of select="concat('255,', (($yPos)+100))"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="citrus:echo">
		<xsl:variable name="yPos" select="150 + (position() * 100)"/>
		<xsl:element name="use">
			<xsl:attribute name="x">200</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:attribute name="xlink:href">#<xsl:value-of select="name()"/></xsl:attribute>
		</xsl:element>
		
		<xsl:element name="text">
			<xsl:attribute name="x">350</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:if test="citrus:message">
				<tspan x="350" dy="20" style="font-size:12px;fill:grey;">
					"<xsl:value-of select="substring(citrus:message, 1, 80)"/>
					<xsl:if test="string-length(citrus:message)>80"> ...</xsl:if>
					"
				</tspan>
			</xsl:if>
		</xsl:element>
		
		<xsl:call-template name="arrow">
			<xsl:with-param name="startPoint"><xsl:value-of select="concat('255,', (($yPos)+60))"/></xsl:with-param>
			<xsl:with-param name="endPoint"><xsl:value-of select="concat('255,', (($yPos)+100))"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="citrus:sleep">
		<xsl:variable name="yPos" select="150 + (position() * 100)"/>
		<xsl:element name="use">
			<xsl:attribute name="x">200</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:attribute name="xlink:href">#<xsl:value-of select="name()"/></xsl:attribute>
		</xsl:element>
		
		<xsl:element name="text">
			<xsl:attribute name="x">350</xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$yPos"/></xsl:attribute>
			<xsl:attribute name="style">font-size:12px;fill:grey;</xsl:attribute>
			<tspan x="350" dy="20"><xsl:value-of select="@time"/> seconds</tspan>
			<xsl:if test="citrus:description">
				<tspan x="350" dy="20" style="font-size:10px;fill:grey;">
					"<xsl:value-of select="substring(citrus:description, 1, 80)"/>
					<xsl:if test="string-length(citrus:description)>80"> ...</xsl:if>
					"
				</tspan>
			</xsl:if>
		</xsl:element>
		
		<xsl:call-template name="arrow">
			<xsl:with-param name="startPoint"><xsl:value-of select="concat('255,', (($yPos)+60))"/></xsl:with-param>
			<xsl:with-param name="endPoint"><xsl:value-of select="concat('255,', (($yPos)+100))"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="word-wrap">
		<xsl:param name="tobewrapped"/>
		<xsl:param name="linelength" select="80"/>
		<xsl:variable name="linelength2" select="($linelength) + 1"/>
		<xsl:choose>
			<xsl:when test="string-length($tobewrapped)>$linelength">
				<tspan x="20" dy="20"><xsl:value-of select="substring($tobewrapped, 1, $linelength)"/></tspan>
				<xsl:call-template name="word-wrap">
					<xsl:with-param name="tobewrapped" select="substring($tobewrapped, $linelength2)"/>
					<xsl:with-param name="linelength" select="$linelength"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<tspan x="20" dy="20"><xsl:value-of select="$tobewrapped"/></tspan>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="arrow">
		<xsl:param name="startPoint"/>
		<xsl:param name="endPoint"/>
		<xsl:element name="path">
			<xsl:attribute name="d">
				<xsl:value-of select="concat('M ', $startPoint , ' L ', $endPoint)"/>
			</xsl:attribute>
			<xsl:attribute name="style">
				fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:2;stroke-linecap:butt;stroke-linejoin:miter;marker-end:url(#arrow);marker-mid:none;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="text()" />

</xsl:stylesheet>
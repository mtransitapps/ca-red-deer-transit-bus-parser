package org.mtransit.parser.ca_red_deer_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRouteSNToIDConverter;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://data.reddeer.ca/gtfsdatafeed
// OLD :https://data.reddeer.ca/data/GTFS/RD_GTFS.zip
// OTHER: https://webmap.reddeer.ca/transit/google_transit.zip
public class RedDeerTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new RedDeerTransitBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Red Deer";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Nullable
	@Override
	public Long convertRouteIdPreviousChars(@NotNull String previousChars) {
		switch (previousChars) {
			case "SNTA": return MRouteSNToIDConverter.startsWith(MRouteSNToIDConverter.other(25L));
		}
		return null;
	}

	private static final Pattern STARTS_WITH_R_ = Pattern.compile("(^(r))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		routeShortName = STARTS_WITH_R_.matcher(routeShortName).replaceAll(EMPTY);
		return super.cleanRouteShortName(routeShortName);
	}

	@Nullable
	@Override
	public String fixColor(@Nullable String color) {
		if (ColorUtils.WHITE.equalsIgnoreCase(color)) {
			return null;
		}
		return super.fixColor(color);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Pattern ROUTE_RSN = Pattern.compile("(route [\\d]+[a-z]?)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWords());
		routeLongName = ROUTE_RSN.matcher(routeLongName).replaceAll(EMPTY);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_RED = "BF311A"; // RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_BOUNDS_ = Pattern.compile("(^(nb|sb|eb|wb) )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
		directionHeadSign = STARTS_WITH_BOUNDS_.matcher(directionHeadSign).replaceAll(EMPTY);
		return directionHeadSign;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("^[\\d]+[a-z]? - ", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_INBOUND_DASH = Pattern.compile("^Inbound - ", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_INBOUND_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"EB", "WB", "NB", "SW",
				"AM", "PM",
				"LTCHS", "SJHS", "HHHS", "NDHS",
				"ND",
		};
	}

	private String[] getIgnoredWordsStop() {
		return new String[]{
				"EB", "WB", "NB", "SW",
				"AM", "PM",
				"LTCHS", "SJHS", "HHHS", "NDHS",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWordsStop());
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode.startsWith("RD")) {
			stopCode = stopCode.substring(2);
		}
		if (stopCode.endsWith("_")) {
			stopCode = stopCode.substring(0, stopCode.length() - 1);
		}
		return Integer.parseInt(stopCode); // use stop code as stop ID
	}
}

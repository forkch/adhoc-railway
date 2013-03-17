package ch.fork.AdHocRailway.services.impl.hibernate.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class HibernateLocomotiveMapper {

	public static Locomotive mapLocomotive(
			HibernateLocomotive hibernateLocomotive) {

		Locomotive locomotive = new Locomotive();
		updateLocomotive(hibernateLocomotive, locomotive);
		return locomotive;
	}

	private static LocomotiveType mapType(String locomotiveType) {
		if (locomotiveType.equalsIgnoreCase("DELTA")) {
			return LocomotiveType.DELTA;
		} else if (locomotiveType.equalsIgnoreCase("DIGITAL")) {
			return LocomotiveType.DIGITAL;
		}
		return null;
	}

	private static String mapType(LocomotiveType locomotiveType) {
		return locomotiveType.toString();
	}

	public static LocomotiveGroup mapHibernateLocomotiveGroup(
			HibernateLocomotiveGroup hLocomotiveGroup) {
		LocomotiveGroup group = new LocomotiveGroup();
		group.setId(hLocomotiveGroup.getId());
		group.setName(hLocomotiveGroup.getName());
		for (HibernateLocomotive hLocomotive : hLocomotiveGroup
				.getLocomotives()) {
			Locomotive locomotive = mapLocomotive(hLocomotive);
			group.addLocomotive(locomotive);
			locomotive.setGroup(group);
		}
		return group;
	}

	public static HibernateLocomotive mapLocomotive(Locomotive locomotive) {
		HibernateLocomotive hLocomotive = new HibernateLocomotive();
		updateHibernateLocomotive(hLocomotive, locomotive);
		return hLocomotive;
	}

	public static HibernateLocomotiveGroup mapLocomotiveGroup(
			LocomotiveGroup locomotiveGroup) {
		HibernateLocomotiveGroup hLocomotiveGroup = new HibernateLocomotiveGroup();
		updateHibernateLocomotiveGroup(hLocomotiveGroup, locomotiveGroup);
		return hLocomotiveGroup;
	}

	public static void updateHibernateLocomotiveGroup(
			HibernateLocomotiveGroup hLocomotiveGroup,
			LocomotiveGroup locomotiveGroup) {
		hLocomotiveGroup.setId(locomotiveGroup.getId());
		hLocomotiveGroup.setName(locomotiveGroup.getName());
	}

	public static void updateLocomotive(
			HibernateLocomotive hibernateLocomotive, Locomotive locomotive) {
		locomotive.setName(hibernateLocomotive.getName());
		locomotive.setId(hibernateLocomotive.getId());
		locomotive.setBus(hibernateLocomotive.getBus());
		locomotive.setDesc(hibernateLocomotive.getDescription());
		locomotive.setImage(hibernateLocomotive.getImage());
		locomotive.setAddress(hibernateLocomotive.getAddress());
		locomotive.setType(mapType(hibernateLocomotive.getType()));
	}

	public static void updateHibernateLocomotive(
			HibernateLocomotive hLocomotive, Locomotive locomotive) {
		hLocomotive.setName(locomotive.getName());
		hLocomotive.setId(locomotive.getId());
		hLocomotive.setAddress(locomotive.getAddress());
		hLocomotive.setBus(locomotive.getBus());
		hLocomotive.setDescription(locomotive.getDesc());
		hLocomotive.setImage(locomotive.getImage());
		hLocomotive.setType(mapType(locomotive.getType()));
	}
}
